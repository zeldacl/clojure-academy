(ns cn.academy.block.network
  (:require [cn.academy.block.env :as env]
            [cn.academy.block.stats :as stats]
            [cn.academy.block.error :as error]
            [cn.academy.block.cache :as cache]
            [clojure.tools.logging :as log]))

;; Network optimization state
(def network-state
  (atom {:bandwidth-usage {}
         :sync-priorities {}
         :batch-queue {}}))

;; Bandwidth tracking
(defn track-bandwidth! [block-id bytes]
  (let [now (System/currentTimeMillis)]
    (swap! network-state update-in 
           [:bandwidth-usage block-id]
           #(conj (or % []) {:timestamp now :bytes bytes}))))

;; Bandwidth calculation
(defn get-bandwidth-usage [block-id window]
  (let [now (System/currentTimeMillis)
        cutoff (- now window)
        usage (filter #(>= (:timestamp %) cutoff)
                     (get-in @network-state [:bandwidth-usage block-id]))]
    (reduce + (map :bytes usage))))

;; Priority calculation
(defn calculate-sync-priority [block]
  (cache/with-cache (:id block) [:sync-priority]
    (let [update-frequency (get-in block [:config :update-frequency] 1)
          importance (get-in block [:config :importance] 1)
          bandwidth-usage (get-bandwidth-usage (:id block) 60000)] ; Last minute
      (* importance 
         (/ update-frequency)
         (/ 1 (max 1 (/ bandwidth-usage 1024)))))))

;; Message batching
(defn add-to-batch! [block-id message]
  (swap! network-state update-in 
         [:batch-queue block-id] 
         #(conj (or % []) message)))

(defn flush-batch! [block-id]
  (when-let [messages (seq (get-in @network-state [:batch-queue block-id]))]
    (let [batch-size (reduce + (map #(count (.getBytes (str %))) messages))]
      (when (<= batch-size (env/get-config [:network :max-batch-size]))
        (mcmod.network/send-message! :batch-update block-id messages)
        (track-bandwidth! block-id batch-size)
        (swap! network-state update :batch-queue dissoc block-id)
        true))))

;; Smart synchronization
(defn should-sync? [block field new-value]
  (let [block-id (:id block)
        threshold (get-in block [:config :sync-thresholds field])]
    (or (nil? threshold)
        (let [old-value (get-in @(:state block) [field])]
          (> (Math/abs (- new-value old-value))
             threshold)))))

(defn optimize-update! [block field new-value]
  (let [block-id (:id block)]
    (when (should-sync? block field new-value)
      (add-to-batch! block-id 
                     {:field field
                      :value new-value})
      (when (>= (count (get-in @network-state [:batch-queue block-id]))
                (env/get-config [:network :batch-threshold]))
        (flush-batch! block-id)))))

;; Periodic batch processing
(defn process-batches! []
  (doseq [[block-id _] (:batch-queue @network-state)]
    (flush-batch! block-id)))

;; Network compression
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

;; Initialize network optimization
(defn init-network! []
  (let [batch-interval (env/get-config [:network :batch-interval] 50)]
    ;; Start batch processing
    (mcmod.scheduler/schedule-periodic
      batch-interval
      process-batches!)
    
    ;; Register network handlers
    (mcmod.network/register-handler! :batch-update
      (fn [{:keys [block-id messages]}]
        (when-let [block (mcmod.block/get-block-by-id block-id)]
          (doseq [{:keys [field value]} messages]
            (swap! (:state block) assoc field value)))))
    
    (log/info "Network optimization system initialized")))