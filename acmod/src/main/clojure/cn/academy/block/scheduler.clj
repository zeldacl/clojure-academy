(ns cn.academy.block.scheduler
  (:require [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Task scheduling state
(def scheduler-state (atom {:tasks {}
                          :task-queue []
                          :current-tick 0}))

;; Task priorities
(def priority-levels
  {:high 0
   :normal 10
   :low 20})

;; Task scheduling
(defn schedule-task! [block-id task-type priority f & args]
  (let [task-id (str block-id "-" (random-uuid))
        task {:id task-id
              :block-id block-id
              :type task-type
              :priority (get priority-levels priority :normal)
              :function f
              :args args
              :scheduled-at (:current-tick @scheduler-state)}]
    (swap! scheduler-state update :tasks assoc task-id task)
    (swap! scheduler-state update :task-queue conj task-id)
    task-id))

;; Task execution
(defn execute-task! [task-id]
  (when-let [task (get-in @scheduler-state [:tasks task-id])]
    (error/with-safe-execution (:block-id task) :scheduler
      (try
        (apply (:function task) (:args task))
        (swap! scheduler-state update :tasks dissoc task-id)
        true
        (catch Exception e
          (log/error "Task execution failed:" task-id (.getMessage e))
          false)))))

;; Task queue processing
(defn process-task-queue! []
  (let [current-tick (swap! scheduler-state update :current-tick inc)
        tasks (sort-by (juxt :priority :scheduled-at) 
                      (map #(get-in @scheduler-state [:tasks %])
                           (:task-queue @scheduler-state)))]
    (swap! scheduler-state assoc :task-queue [])
    (doseq [task tasks]
      (execute-task! (:id task)))))

;; Periodic task scheduling
(defn schedule-periodic! [block-id task-type interval f & args]
  (let [task-id (schedule-task! block-id task-type :normal
                 (fn [interval f & args]
                   (apply f args)
                   (apply schedule-periodic! block-id task-type interval f args))
                 interval f args)]
    task-id))

;; Task management
(defn cancel-task! [task-id]
  (swap! scheduler-state update :tasks dissoc task-id)
  (swap! scheduler-state update :task-queue 
         (fn [queue] (filterv #(not= % task-id) queue))))

(defn cancel-block-tasks! [block-id]
  (let [block-tasks (->> (:tasks @scheduler-state)
                        (filter #(= (:block-id (val %)) block-id))
                        (map key))]
    (doseq [task-id block-tasks]
      (cancel-task! task-id))))