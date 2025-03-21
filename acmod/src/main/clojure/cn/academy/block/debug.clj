(ns cn.academy.block.debug
  (:require [cn.academy.block.validation :as validation]
            [clojure.tools.logging :as log]
            [clojure.pprint :as pp]))

;; Debug state tracking
(def debug-state (atom {:enabled false
                       :tracked-blocks #{}
                       :performance-metrics {}}))

;; Monitoring configuration
(def monitor-config
  {:sample-rate 20  ; ticks
   :history-size 100
   :metrics #{:energy :progress :efficiency}})

;; Debug helpers
(defn enable-debug! []
  (swap! debug-state assoc :enabled true))

(defn disable-debug! []
  (swap! debug-state assoc :enabled false))

;; Block tracking
(defn track-block! [block]
  (swap! debug-state update :tracked-blocks conj block))

(defn untrack-block! [block]
  (swap! debug-state update :tracked-blocks disj block))

;; Performance monitoring
(defn record-metric! [block-type metric value]
  (when (:enabled @debug-state)
    (swap! debug-state update-in [:performance-metrics block-type metric]
           (fn [history]
             (take (:history-size monitor-config)
                   (conj (or history '()) value))))))

;; State monitoring
(defn monitor-block-state! [block]
  (when (and (:enabled @debug-state)
             (contains? (:tracked-blocks @debug-state) block))
    (let [state @(:state block)]
      (doseq [metric (:metrics monitor-config)]
        (when-let [value (get state metric)]
          (record-metric! (:type block) metric value))))))

;; Debug reporting
(defn generate-block-report [block]
  (let [state @(:state block)
        config (:config block)]
    {:type (:type block)
     :state state
     :config config
     :validation (validation/validate-machine! block)
     :metrics (get-in @debug-state [:performance-metrics (:type block)])}))

(defn print-block-report! [block]
  (when (:enabled @debug-state)
    (let [report (generate-block-report block)]
      (log/debug "Block Report:")
      (pp/pprint report))))

;; Performance analysis
(defn analyze-performance! [block-type]
  (when-let [metrics (get-in @debug-state [:performance-metrics block-type])]
    (into {}
          (for [[metric values] metrics]
            [metric {:min (apply min values)
                    :max (apply max values)
                    :avg (/ (apply + values) (count values))}]))))