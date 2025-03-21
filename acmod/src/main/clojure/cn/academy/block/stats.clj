(ns cn.academy.block.stats
  (:require [mcmod.protocols :refer [IStatsCollector IStatistic]]
            [cn.academy.block.error :as error]
            [cn.academy.block.scheduler :as scheduler]
            [clojure.tools.logging :as log]))

;; Stats state tracking
(def stats-state
  (atom {:counters {}
         :timers {}
         :gauges {}}))

;; Statistic implementation  
(defrecord Statistic [id type value-atom]
  IStatistic
  (get-value [_]
    @value-atom)
  
  (increment! [_]
    (swap! value-atom inc))
  
  (decrement! [_]
    (swap! value-atom dec))
  
  (add! [_ amount]
    (swap! value-atom + amount))
  
  (set-value! [_ value]
    (reset! value-atom value))
  
  (reset! [_]
    (reset! value-atom 0)))

;; Stats collector implementation
(defrecord StatsCollector [category state-atom]
  IStatsCollector
  (register-counter! [_ id]
    (let [counter (->Statistic id :counter (atom 0))]
      (swap! stats-state assoc-in [:counters category id] counter)
      counter))
  
  (register-timer! [_ id]
    (let [timer (->Statistic id :timer (atom {}))]
      (swap! stats-state assoc-in [:timers category id] timer)
      timer))
  
  (register-gauge! [_ id]
    (let [gauge (->Statistic id :gauge (atom 0))]
      (swap! stats-state assoc-in [:gauges category id] gauge)
      gauge))
  
  (get-counter [_ id]
    (get-in @stats-state [:counters category id]))
  
  (get-timer [_ id]
    (get-in @stats-state [:timers category id]))
  
  (get-gauge [_ id]
    (get-in @stats-state [:gauges category id]))
  
  (get-all-stats [_]
    {:counters (get-in @stats-state [:counters category])
     :timers (get-in @stats-state [:timers category])
     :gauges (get-in @stats-state [:gauges category])}))

;; Factory functions
(defn create-collector [category]
  (->StatsCollector category stats-state))

;; Timer helpers
(defmacro with-timer [timer & body]
  `(let [start# (System/currentTimeMillis)
         result# (do ~@body)
         duration# (- (System/currentTimeMillis) start#)]
     (swap! (:value-atom ~timer) 
            (fn [times#]
              (let [count# (inc (get times# :count 0))
                    total# (+ (get times# :total 0) duration#)
                    min# (min (get times# :min Long/MAX_VALUE) duration#)
                    max# (max (get times# :max 0) duration#)]
                {:count count#
                 :total total#
                 :min min#
                 :max max#
                 :avg (double (/ total# count#))})))
     result#))

;; Metric tracking helpers
(defn track-machine-operation! [collector machine-id op-type]
  (when-let [counter (.get-counter collector (str machine-id "-" op-type))]
    (.increment! counter)))

(defn track-resource-usage! [collector resource-type amount]
  (when-let [gauge (.get-gauge collector (str "resource-" resource-type))]
    (.add! gauge amount)))

(defn track-network-traffic! [collector packet-type size]
  (when-let [counter (.get-counter collector (str "network-" packet-type))]
    (.increment! counter))
  (when-let [gauge (.get-gauge collector "network-bandwidth")]
    (.add! gauge size)))

;; Stats reporting
(defn generate-stats-report [collector]
  (let [{:keys [counters timers gauges]} (.get-all-stats collector)]
    {:timestamp (System/currentTimeMillis)
     :counters (into {} (map (fn [[k v]] [k (.get-value v)]) counters))
     :timers (into {} (map (fn [[k v]] [k (.get-value v)]) timers))
     :gauges (into {} (map (fn [[k v]] [k (.get-value v)]) gauges))}))

;; Initialize stats system
(defn init-stats! []
  (reset! stats-state {:counters {}
                       :timers {}
                       :gauges {}})
  
  ;; Create default collectors
  (let [machine-stats (create-collector :machine)
        network-stats (create-collector :network)
        resource-stats (create-collector :resource)]
    
    ;; Register default stats
    (.register-counter! machine-stats "total-operations")
    (.register-timer! machine-stats "operation-time")
    (.register-gauge! machine-stats "active-machines")
    
    (.register-counter! network-stats "packets-sent")
    (.register-counter! network-stats "packets-received")
    (.register-gauge! network-stats "network-bandwidth")
    
    (.register-gauge! resource-stats "energy-usage")
    (.register-gauge! resource-stats "fluid-usage")
    
    ;; Schedule periodic stats reporting
    (scheduler/schedule-periodic! 
     "stats-reporter" :system 60000
     #(log/info "Stats report:"
                (generate-stats-report machine-stats)
                (generate-stats-report network-stats)
                (generate-stats-report resource-stats)))))