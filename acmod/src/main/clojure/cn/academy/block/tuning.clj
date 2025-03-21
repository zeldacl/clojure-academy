(ns cn.academy.block.tuning
  (:require [cn.academy.block.performance :as perf]
            [cn.academy.block.env :as env]
            [cn.academy.block.stats :as stats]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Tuning parameters
(def tuning-parameters
  {:energy-production {:min 0.1
                      :max 2.0
                      :step 0.1
                      :metric :energy-efficiency}
   :process-speed {:min 0.5
                   :max 3.0
                   :step 0.1
                   :metric :processing-speed}
   :update-rate {:min 1
                 :max 20
                 :step 1
                 :metric :tick-time}})

;; Parameter state tracking
(def tuning-state
  (atom {:block-params {}
         :history {}
         :optimization-active false}))

;; Performance thresholds
(def performance-thresholds
  {:tick-time 50  ; ms
   :memory-usage 0.8  ; 80% of max
   :tps 18.0})

;; Parameter adjustment
(defn adjust-parameter! [block-id param-key delta]
  (let [param-config (get tuning-parameters param-key)
        current-value (get-in @tuning-state [:block-params block-id param-key] 1.0)
        new-value (-> current-value
                     (+ delta)
                     (max (:min param-config))
                     (min (:max param-config)))]
    (swap! tuning-state assoc-in [:block-params block-id param-key] new-value)
    new-value))

;; Performance evaluation
(defn evaluate-performance [block-id param-key]
  (let [param-config (get tuning-parameters param-key)
        metric-key (:metric param-config)
        current-stats (stats/get-block-stats block-id metric-key)]
    {:value (:average current-stats)
     :trend (- (:average current-stats)
               (get-in @tuning-state [:history block-id param-key :last-value] 0))}))

;; Optimization logic
(defn optimize-parameter! [block-id param-key]
  (error/with-safe-execution block-id :tuning
    (let [performance (evaluate-performance block-id param-key)
          current-value (get-in @tuning-state [:block-params block-id param-key] 1.0)]
      (cond
        ;; Positive trend - continue in same direction
        (pos? (:trend performance))
        (let [last-delta (get-in @tuning-state [:history block-id param-key :last-delta] 0)
              new-delta (if (zero? last-delta)
                         (get-in tuning-parameters [param-key :step])
                         last-delta)]
          (adjust-parameter! block-id param-key new-delta))
        
        ;; Negative trend - reverse direction
        (neg? (:trend performance))
        (let [last-delta (get-in @tuning-state [:history block-id param-key :last-delta] 0)
              new-delta (* -0.5 last-delta)] ; Reduce step size and reverse
          (adjust-parameter! block-id param-key new-delta))
        
        ;; No change - try small adjustment
        :else
        (adjust-parameter! block-id param-key 
                          (get-in tuning-parameters [param-key :step]))))
    
    ;; Update history
    (swap! tuning-state update-in [:history block-id param-key]
           assoc
           :last-value (get-in (evaluate-performance block-id param-key) [:value])
           :last-delta (get-in tuning-parameters [param-key :step]))))

;; Block optimization
(defn optimize-block! [block]
  (let [block-id (:id block)]
    (doseq [param-key (keys tuning-parameters)]
      (optimize-parameter! block-id param-key))))

;; System monitoring
(defn check-system-load []
  (and (< (perf/get-average-tick-time) 
          (:tick-time performance-thresholds))
       (< (perf/get-memory-usage)
          (:memory-usage performance-thresholds))
       (> (perf/get-tps)
          (:tps performance-thresholds))))

;; Tuning cycle
(defn run-tuning-cycle! []
  (when (check-system-load)
    (swap! tuning-state assoc :optimization-active true)
    (doseq [block (mcmod.block/get-loaded-blocks)]
      (optimize-block! block))
    (swap! tuning-state assoc :optimization-active false)))

;; Initialize tuning system
(defn init-tuning! []
  (let [interval (env/get-config [:tuning :interval] 300)]
    (mcmod.scheduler/schedule-periodic
      interval
      run-tuning-cycle!)))