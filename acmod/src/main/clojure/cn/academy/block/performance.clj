(ns cn.academy.block.performance
  (:require [cn.academy.block.scheduler :as scheduler]
            [cn.academy.block.debug :as debug]
            [clojure.tools.logging :as log]))

;; Performance metrics
(def performance-state
  (atom {:metrics {}
         :thresholds {:tick-time 50  ; ms
                     :active-blocks 1000
                     :updates-per-tick 100}
         :optimization-active false}))

;; Metric collection
(defn record-metric! [block-id metric-type value]
  (swap! performance-state update-in [:metrics block-id metric-type]
         (fn [history]
           (take 100 (conj (or history []) 
                          {:value value
                           :timestamp (System/currentTimeMillis)})))))

;; Performance analysis
(defn analyze-block-performance [block-id]
  (let [metrics (get-in @performance-state [:metrics block-id])]
    {:avg-tick-time (when-let [times (:tick times metrics)]
                     (/ (apply + (map :value times))
                        (count times)))
     :update-frequency (count (:updates metrics))
     :last-active (when-let [updates (:updates metrics)]
                   (:timestamp (last updates)))}))

;; Optimization strategies
(defn optimize-block-updates! [block]
  (let [perf (analyze-block-performance (:id block))
        tick-threshold (get-in @performance-state [:thresholds :tick-time])]
    (when (and (:avg-tick-time perf)
               (> (:avg-tick-time perf) tick-threshold))
      (scheduler/schedule-task! (:id block) :optimization :low
        (fn [block]
          (swap! (:state block) update :update-interval inc)
          (debug/record-metric! (:type block) :optimization-applied true))
        block))))

;; Load balancing
(defn balance-block-load! []
  (let [active-blocks (count (:metrics @performance-state))
        threshold (get-in @performance-state [:thresholds :active-blocks])]
    (when (> active-blocks threshold)
      (doseq [[block-id metrics] (:metrics @performance-state)]
        (when-let [block (mcmod.block/get-block-by-id block-id)]
          (optimize-block-updates! block))))))

;; Performance monitoring tasks
(defn start-performance-monitoring! []
  (scheduler/schedule-periodic! "performance-monitor" :system 100
    (fn []
      (when-not (:optimization-active @performance-state)
        (balance-block-load!)))))

;; Block performance tracking
(defn track-block-performance! [block]
  (let [block-id (:id block)]
    (scheduler/schedule-periodic! block-id :monitoring 20
      (fn [block]
        (let [start-time (System/currentTimeMillis)]
          (try
            (mcmod.block/update! block)
            (let [tick-time (- (System/currentTimeMillis) start-time)]
              (record-metric! block-id :tick tick-time)
              (record-metric! block-id :updates 1))
            (catch Exception e
              (log/error "Error updating block:" block-id (.getMessage e)))))
        block))))