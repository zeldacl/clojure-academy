(ns cn.academy.block.automation
  (:require [cn.academy.block.scheduler :as scheduler]
            [cn.academy.block.resource :as resource]
            [cn.academy.block.security :as security]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Automation task types
(def task-types
  #{:resource-transfer
    :block-interaction
    :recipe-processing
    :energy-distribution
    :inventory-management})

;; Task definitions
(defprotocol IAutomationTask
  (can-execute? [this])
  (execute! [this])
  (on-complete [this result])
  (on-failure [this error]))

;; Task state tracking
(def automation-state
  (atom {:tasks {}
         :schedules {}
         :active-automations {}}))

;; Task implementation
(defrecord AutomationTask [id type source target params]
  IAutomationTask
  (can-execute? [_]
    (case type
      :resource-transfer
      (resource/validate-transfer! source target (:resource params) (:amount params))
      
      :block-interaction
      (security/check-permission! source target :interact)
      
      :recipe-processing
      (when-let [recipe (:recipe params)]
        (mcmod.recipe/can-process? source recipe))
      
      :energy-distribution
      (pos? (resource/extract-resource! source :energy (:amount params)))
      
      :inventory-management
      (let [{:keys [slot-from slot-to amount]} params]
        (and (resource/can-extract? source :item amount)
             (resource/can-accept? target :item amount)))))
  
  (execute! [this]
    (error/with-safe-execution id :automation
      (case type
        :resource-transfer
        (resource/transfer-resource! source target (:resource params) (:amount params))
        
        :block-interaction
        (mcmod.block/interact! source target (:interaction params))
        
        :recipe-processing
        (when-let [recipe (:recipe params)]
          (mcmod.recipe/process! source recipe))
        
        :energy-distribution
        (resource/distribute-resources! source :energy)
        
        :inventory-management
        (let [{:keys [slot-from slot-to amount]} params]
          (resource/transfer-resource! source target :item amount)))))
  
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
                         (when (can-execute? task)
                           (execute! task)))))]
    (swap! automation-state assoc-in [:schedules task-id] schedule-id)
    schedule-id))

;; Automation creation
(defn create-automation! [type source target params interval]
  (let [task-id (create-task! type source target params)]
    (schedule-task! task-id interval)
    (swap! automation-state assoc-in 
           [:active-automations task-id]
           {:type type
            :source source
            :target target
            :params params
            :interval interval})
    task-id))

;; Automation management
(defn stop-automation! [task-id]
  (when-let [schedule-id (get-in @automation-state [:schedules task-id])]
    (scheduler/cancel-task! schedule-id)
    (swap! automation-state update :schedules dissoc task-id)
    (swap! automation-state update :active-automations dissoc task-id)
    true))

(defn list-automations []
  (:active-automations @automation-state))

;; Initialize automation system
(defn init-automation! []
  (scheduler/schedule-periodic! "automation-monitor" :system 60
    (fn []
      (doseq [[task-id automation] (:active-automations @automation-state)]
        (when-let [task (get-in @automation-state [:tasks task-id])]
          (when-not (can-execute? task)
            (log/warn "Automation task disabled - cannot execute:" task-id)
            (stop-automation! task-id)))))))