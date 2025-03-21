(ns cn.academy.block.automation
  (:require [cn.academy.block.scheduler :as scheduler]
            [cn.academy.block.error :as error]
            [mcmod.protocols :refer [IAutomation IResourceTransfer]]
            [clojure.tools.logging :as log]))

;; Automation state tracking
(def automation-state
  (atom {:tasks {}
         :schedules {}}))

;; Task implementation
(defrecord AutomationTask [id type source target params]
  IAutomation
  (validate-task [_]
    (case type
      :resource-transfer
      (and (satisfies? IResourceTransfer source)
           (satisfies? IResourceTransfer target)
           (.can-transfer? source target (:resource params) (:amount params)))
      
      :block-interaction
      (.can-interact? source target (:interaction params))
      
      :recipe-processing
      (when-let [recipe (:recipe params)]
        (.can-process? source recipe))
      
      false))
  
  (execute-task [this]
    (error/with-safe-execution id :automation
      (case type
        :resource-transfer
        (.transfer! source target (:resource params) (:amount params))
        
        :block-interaction
        (.interact! source target (:interaction params))
        
        :recipe-processing
        (when-let [recipe (:recipe params)]
          (.process! source recipe)))))
  
  (on-complete [_ result]
    (scheduler/schedule-task! id :automation :normal
      #(log/info "Automation task completed:" id "with result:" result)))
  
  (on-failure [_ error]
    (log/error "Automation task failed:" id error)))

;; Task creation
(defn create-task! [type source target params]
  (let [task-id (str (random-uuid))
        task (->AutomationTask task-id type source target params)]
    (swap! automation-state assoc-in [:tasks task-id] task)
    task-id))

;; Task scheduling
(defn schedule-task! [task-id interval]
  (let [schedule-id (scheduler/schedule-periodic! 
                     task-id :automation interval
                     (fn []
                       (when-let [task (get-in @automation-state [:tasks task-id])]
                         (when (.validate-task task)
                           (.execute-task task)))))]
    (swap! automation-state assoc-in [:schedules task-id] schedule-id)
    schedule-id))

;; Task management
(defn stop-task! [task-id]
  (when-let [schedule-id (get-in @automation-state [:schedules task-id])]
    (scheduler/cancel-task! schedule-id)
    (swap! automation-state update :schedules dissoc task-id)
    true))

;; Initialize automation system
(defn init-automation! []
  (scheduler/schedule-periodic! "automation-monitor" :system 60
    (fn []
      (doseq [[task-id task] (:tasks @automation-state)]
        (when-not (.validate-task task)
          (log/warn "Automation task invalid - stopping:" task-id)
          (stop-task! task-id))))))