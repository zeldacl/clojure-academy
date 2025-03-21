(ns cn.academy.block.monitor
  (:require [clojure.tools.logging :as log]
            [cn.academy.block.config :as config]
            [cn.academy.block.notify :as notify])
  (:import [java.lang.management ManagementFactory]))

;; Metric storage
(def metrics-state
  (atom {:metrics {}
         :thresholds {}
         :collectors {}}))

;; Metric types
(def metric-types
  #{:counter :gauge :histogram})

;; Basic metric record
(defrecord Metric [type name category value timestamp tags])

;; Metric collection functions
(defn record-metric! [name category value & [tags]]
  (let [metric (->Metric :gauge name category value 
                        (System/currentTimeMillis) 
                        (or tags {}))]
    (swap! metrics-state update-in [:metrics name] 
           (fn [entries]
             (take 1000 (conj (or entries []) metric))))
    metric))

(defn increment-counter! [name category & [tags]]
  (let [current (or (-> @metrics-state :metrics name last :value) 0)
        metric (record-metric! name category (inc current) tags)]
    (check-thresholds! name)
    metric))

;; Threshold management
(defn set-threshold! [metric-name threshold-fn callback]
  (swap! metrics-state assoc-in [:thresholds metric-name] 
         {:fn threshold-fn
          :callback callback}))

(defn check-thresholds! [metric-name]
  (when-let [{:keys [fn callback]} 
             (get-in @metrics-state [:thresholds metric-name])]
    (let [current-value (-> @metrics-state :metrics metric-name last :value)]
      (when (fn current-value)
        (callback metric-name current-value)))))

;; System metric collectors
(def system-collectors
  {"memory" (fn []
             (let [runtime (Runtime/getRuntime)
                   max-memory (.maxMemory runtime)
                   total-memory (.totalMemory runtime)
                   free-memory (.freeMemory runtime)
                   used-memory (- total-memory free-memory)]
               (record-metric! "memory.used" :system used-memory)
               (record-metric! "memory.total" :system total-memory)
               (record-metric! "memory.max" :system max-memory)
               (record-metric! "memory.free" :system free-memory)))

   "threads" (fn []
              (let [thread-mx (ManagementFactory/getThreadMXBean)]
                (record-metric! "threads.count" :system 
                              (.getThreadCount thread-mx))
                (record-metric! "threads.peak" :system
                              (.getPeakThreadCount thread-mx))))
   
   "gc" (fn []
          (doseq [gc-bean (ManagementFactory/getGarbageCollectorMXBeans)]
            (record-metric! (str "gc." (.getName gc-bean) ".count")
                          :system
                          (.getCollectionCount gc-bean))
            (record-metric! (str "gc." (.getName gc-bean) ".time")
                          :system
                          (.getCollectionTime gc-bean))))})

;; Metric querying
(defn get-metrics 
  ([] (:metrics @metrics-state))
  ([name] (get-in @metrics-state [:metrics name]))
  ([name & {:keys [from to]}]
   (let [metrics (get-in @metrics-state [:metrics name])]
     (cond->> metrics
       from (filter #(>= (:timestamp %) from))
       to (filter #(<= (:timestamp %) to))))))

;; Collector management
(defn register-collector! [name collector-fn interval]
  (when-let [existing (get-in @metrics-state [:collectors name])]
    (future-cancel (:future existing)))
  
  (let [running (atom true)
        collector-future 
        (future
          (while @running
            (try
              (collector-fn)
              (catch Exception e
                (log/error "Collector failed:" (.getMessage e))))
            (Thread/sleep interval)))]
    
    (swap! metrics-state assoc-in [:collectors name]
           {:future collector-future
            :running running})))

(defn stop-collector! [name]
  (when-let [{:keys [running]} (get-in @metrics-state [:collectors name])]
    (reset! running false)))

;; System initialization
(defn init-monitoring! []
  ;; Reset state
  (reset! metrics-state {:metrics {}
                        :thresholds {}
                        :collectors {}})
  
  ;; Register system collectors
  (doseq [[name collector-fn] system-collectors]
    (register-collector! name collector-fn 
                        (config/get-config [:monitor :interval] 5000)))
  
  ;; Set up basic thresholds
  (set-threshold! "memory.used"
                 (fn [value]
                   (let [max-mem (-> @metrics-state 
                                   :metrics 
                                   (get-in ["memory.max" 0 :value]))]
                     (> value (* max-mem 0.9))))
                 (fn [name value]
                   (notify/warn! (format "High memory usage: %d bytes" value)
                               {:metric name :value value})))
  
  ;; Set up periodic cleanup
  (register-collector! "cleanup"
                      (fn []
                        (let [cutoff (- (System/currentTimeMillis)
                                      (* 1000 60 60 24))] ; 24h retention
                          (swap! metrics-state update :metrics
                                 (fn [metrics]
                                   (reduce-kv
                                    (fn [m k v]
                                      (assoc m k
                                             (filterv #(> (:timestamp %) cutoff) v)))
                                    {}
                                    metrics)))))
                      (* 1000 60 60))) ; Run hourly

;; Shutdown
(defn shutdown-monitoring! []
  (doseq [name (keys (:collectors @metrics-state))]
    (stop-collector! name)))