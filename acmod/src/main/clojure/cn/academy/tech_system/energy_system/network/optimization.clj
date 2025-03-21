(ns cn.academy.tech-system.energy-system.network.optimization
  (:require [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.network.wireless :as network]
            [mcmod.scheduler :as scheduler]
            [clojure.tools.logging :as log]))

(def ^:private network-state
  (atom {:bandwidth-usage {}
         :batch-queue {}
         :sync-priorities {}
         :compression true}))

;; Extended compression support
(defprotocol ICompressible
  (compress [this])
  (decompress [this data]))

(defrecord DeltaCompression []
  ICompressible
  (compress [_]
    (fn [old-value new-value]
      (if (number? new-value)
        (- new-value old-value)
        new-value)))
  
  (decompress [_ data]
    (fn [old-value delta]
      (if (number? old-value)
        (+ old-value delta)
        delta))))

;; Smart synchronization
(defn- should-sync? [node field new-value]
  (let [node-id (:id node)
        threshold (get-in @network-state [:sync-thresholds node-id field])]
    (or (nil? threshold)
        (let [old-value (get-in node [field])]
          (> (Math/abs (- new-value old-value))
             threshold)))))

(defn- track-bandwidth! [node-id bytes]
  (let [now (System/currentTimeMillis)]
    (swap! network-state update-in 
           [:bandwidth-usage node-id]
           #(conj (or % []) {:timestamp now :bytes bytes}))))

(defn get-bandwidth-usage [node-id window]
  (let [now (System/currentTimeMillis)
        cutoff (- now window)
        usage (filter #(>= (:timestamp %) cutoff)
                     (get-in @network-state [:bandwidth-usage node-id]))]
    (reduce + (map :bytes usage))))

;; Enhanced batch processing with priorities
(defn- calculate-sync-priority [node]
  (let [update-frequency (get-in node [:config :update-frequency] 1)
        importance (get-in node [:config :importance] 1)
        bandwidth-usage (get-bandwidth-usage (:id node) 60000)]
    (* importance 
       (/ update-frequency)
       (/ 1 (max 1 (/ bandwidth-usage 1024))))))

(defn- add-to-batch! [node-id message]
  (swap! network-state update-in 
         [:batch-queue node-id] 
         #(conj (or % []) message)))

(defn- flush-batch! [node-id]
  (when-let [messages (seq (get-in @network-state [:batch-queue node-id]))]
    (let [batch-size (reduce + (map #(count (.getBytes (str %))) messages))]
      (when (<= batch-size 32768) ; Max batch size 32KB
        (network/send-batch! node-id messages)
        (track-bandwidth! node-id batch-size)
        (swap! network-state update :batch-queue dissoc node-id)
        true))))

(defn optimize-update! [node field new-value]
  (let [node-id (:id node)]
    (when (should-sync? node field new-value)
      (add-to-batch! node-id 
                     {:field field
                      :value new-value})
      (when (>= (count (get-in @network-state [:batch-queue node-id]))
                (get-in @network-state [:config :batch-threshold] 10))
        (flush-batch! node-id)))))

(defn- process-batches! []
  (doseq [[node-id _] (:batch-queue @network-state)]
    (flush-batch! node-id)))

(defn init! []
  (scheduler/schedule-recurring 50 process-batches!)
  (log/info "Network optimization system initialized with enhanced features"))