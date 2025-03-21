(ns cn.academy.block.scheduler
  (:require [mcmod.protocols :refer [IScheduler ITask]]
            [cn.academy.block.error :as error]
            [cn.academy.block.monitor :as monitor]
            [cn.academy.block.profile :as profile]
            [cn.academy.block.stats :as stats]
            [clojure.tools.logging :as log])
  (:import [java.util.concurrent Executors ScheduledFuture TimeUnit]))

;; Scheduler state tracking
(def scheduler-state
  (atom {:executor nil
         :tasks {}
         :futures {}}))

;; Task implementation
(defrecord ScheduledTask [id category interval task-fn state-atom]
  ITask
  (execute! [_]
    (error/with-error-handling :scheduler
      (profile/with-profile (str "task-" id) category
        (task-fn))))
  
  (cancel! [_]
    (when-let [future (get-in @state-atom [:futures id])]
      (.cancel ^ScheduledFuture future false)
      (swap! state-atom update :futures dissoc id)))
  
  (is-running? [_]
    (when-let [future (get-in @state-atom [:futures id])]
      (not (.isDone ^ScheduledFuture future)))))

;; Scheduler implementation
(defrecord BlockScheduler [state-atom]
  IScheduler
  (schedule! [_ id category interval task-fn]
    (let [task (->ScheduledTask id category interval task-fn state-atom)
          executor (:executor @state-atom)
          future (.scheduleAtFixedRate executor
                                     #(.execute! task)
                                     0 interval TimeUnit/MILLISECONDS)]
      (swap! state-atom assoc-in [:tasks id] task)
      (swap! state-atom assoc-in [:futures id] future)
      task))
  
  (schedule-once! [_ id category delay task-fn]
    (let [task (->ScheduledTask id category delay task-fn state-atom)
          executor (:executor @state-atom)
          future (.schedule executor
                          #(.execute! task)
                          delay TimeUnit/MILLISECONDS)]
      (swap! state-atom assoc-in [:tasks id] task)
      (swap! state-atom assoc-in [:futures id] future)
      task))
  
  (cancel-task! [_ id]
    (when-let [task (get-in @state-atom [:tasks id])]
      (.cancel! task)
      (swap! state-atom update :tasks dissoc id)))
  
  (get-task [_ id]
    (get-in @state-atom [:tasks id]))
  
  (get-running-tasks [_]
    (->> (:tasks @state-atom)
         (filter (fn [[_ task]] (.is-running? task)))
         (into {}))))

;; Factory functions
(defn create-scheduler []
  (->BlockScheduler scheduler-state))

;; Task definition helpers
(defn periodic-task [id category interval f]
  {:id id
   :category category
   :interval interval
   :task f})

(defn delayed-task [id category delay f]
  {:id id
   :category category
   :delay delay
   :task f})

;; Standard maintenance tasks
(def maintenance-tasks
  [(periodic-task "stats-collector" :system 60000
                 #(stats/collect-system-stats!))
   
   (periodic-task "cache-cleanup" :system 300000
                 #(do (monitor/cleanup-old-metrics!)
                     (stats/cleanup-old-stats!)))
   
   (periodic-task "error-cleanup" :system 3600000
                 #(error/cleanup-old-errors!))
   
   (periodic-task "profile-report" :system 600000
                 #(doseq [category [:machine :network :world]]
                    (let [report (profile/generate-profile-report category)]
                      (log/debug "Profile report for" category ":" report))))])

;; Background task monitoring
(defn check-task-health! []
  (let [scheduler (create-scheduler)
        tasks (.get-running-tasks scheduler)]
    (doseq [[id task] tasks]
      (when-not (.is-running? task)
        (log/warn "Task" id "not running, attempting restart")
        (.cancel-task! scheduler id)
        (when-let [{:keys [category interval task-fn]} (get @scheduler-state [:tasks id])]
          (.schedule! scheduler id category interval task-fn))))))

;; Initialize scheduler system
(defn init-scheduler! []
  (when-let [old-executor (:executor @scheduler-state)]
    (.shutdownNow old-executor))
  
  (reset! scheduler-state {:executor (Executors/newScheduledThreadPool 4)
                          :tasks {}
                          :futures {}})
  
  (let [scheduler (create-scheduler)]
    ;; Schedule maintenance tasks
    (doseq [{:keys [id category interval task]} maintenance-tasks]
      (.schedule! scheduler id category interval task))
    
    ;; Schedule health check
    (.schedule! scheduler 
               "health-check" :system 300000 
               check-task-health!)))