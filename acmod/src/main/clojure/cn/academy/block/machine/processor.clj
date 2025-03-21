(ns cn.academy.block.machine.processor
  (:require [cn.academy.block.machine.core :as machine]
            [cn.academy.core.monitoring :as monitoring]
            [cn.academy.core.profiling :as profiling]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Processing state tracking
(def processor-state
  (atom {:active-processes {}}))

;; Machine processing implementation
(defn start-process!
  "Start machine processing"
  [machine recipe]
  (error/with-error-handling (:id machine) :process
    (profiling/with-profile "machine" "start-process"
      (when-not (get-in @processor-state [:active-processes (:id machine)])
        (let [process {:machine machine
                      :recipe recipe
                      :start-time (System/currentTimeMillis)
                      :progress 0}]
          (swap! processor-state assoc-in 
                 [:active-processes (:id machine)] 
                 process))))))

(defn update-process!
  "Update machine process state"
  [process]
  (profiling/with-profile "machine" "update-process"
    (let [{:keys [machine recipe start-time]} process
          current-time (System/currentTimeMillis)
          elapsed (- current-time start-time)
          required-time (:processing-time recipe)
          progress (min 1.0 (/ elapsed required-time))]
      (monitoring/with-metrics "machine" "process-progress"
        (assoc process :progress progress)))))

(defn complete-process!
  "Complete machine process"
  [process]
  (profiling/with-profile "machine" "complete-process"
    (let [{:keys [machine recipe]} process]
      ;; Record processing time
      (monitoring/with-metrics "machine" "process-time"
        ;; Apply recipe outputs
        (doseq [[resource amount] (:outputs recipe)]
          (machine/add-resource! machine resource amount))
        ;; Clear process state
        (swap! processor-state update :active-processes 
               dissoc (:id machine))))))

;; Process tick handler
(defn tick-processes!
  "Update all active processes"
  []
  (profiling/with-profile "machine" "tick"
    (doseq [[id process] (:active-processes @processor-state)]
      (let [updated (update-process! process)]
        (if (>= (:progress updated) 1.0)
          (complete-process! updated)
          (swap! processor-state assoc-in 
                 [:active-processes id] 
                 updated))))))

;; Machine error handling
(defn handle-process-error!
  "Handle process error"
  [machine error]
  (error/with-error-handling (:id machine) :process
    ;; Stop the process
    (swap! processor-state update :active-processes 
           dissoc (:id machine))
    ;; Report error
    (machine/handle-error! machine error)))

;; Process monitoring
(defn get-process-metrics []
  (let [processes (:active-processes @processor-state)]
    {:active-count (count processes)
     :progress (into {} 
                (for [[id process] processes]
                  [id (:progress process)]))}))

;; Initialize processor
(defn init-processor! []
  (reset! processor-state {:active-processes {}})
  ;; Start monitoring
  (monitoring/track-rate "machine" "processes-per-second" 1000
    (tick-processes!)))