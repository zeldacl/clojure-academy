(ns cn.academy.block.machine.core
  (:require [mcmod.protocols :refer [IMachine IEnergyStorage IResourceHandler]]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Machine state tracking
(def machine-registry
  (atom {:types {}
         :instances {}}))

;; Core machine implementation
(defrecord Machine [id config state-atom]
  IMachine
  (get-id [_] id)
  
  (get-type [_] (:type config))
  
  (is-active? [_] 
    (:active @state-atom))
  
  (start! [this]
    (swap! state-atom assoc :active true)
    (error/clear-errors! id))
  
  (stop! [this]
    (swap! state-atom assoc :active false))
  
  IEnergyStorage
  (get-energy-stored [_]
    (get @state-atom :energy 0))
  
  (get-max-energy-stored [_]
    (get-in config [:limits :energy]))
  
  (receive-energy [_ amount simulate]
    (let [current (get @state-atom :energy 0)
          max-energy (get-in config [:limits :energy])
          accepted (min amount (- max-energy current))]
      (when-not simulate
        (swap! state-atom update :energy + accepted))
      accepted))
  
  (extract-energy [_ amount simulate]
    (let [current (get @state-atom :energy 0)
          extracted (min amount current)]
      (when-not simulate
        (swap! state-atom update :energy - extracted))
      extracted))

  IResourceHandler
  (has-resources? [_ resources]
    (every? (fn [[type amount]]
              (>= (get-in @state-atom [:resources type] 0) amount))
            resources))
  
  (add-resources! [_ resources]
    (doseq [[type amount] resources]
      (swap! state-atom update-in [:resources type] #(+ (or % 0) amount))))
  
  (consume-resources! [_ resources]
    (doseq [[type amount] resources]
      (swap! state-atom update-in [:resources type] #(- (or % 0) amount)))))

;; Machine registration and creation
(defn register-machine-type!
  "Register a new machine type configuration"
  [type config]
  (swap! machine-registry assoc-in [:types type] config))

(defn create-machine!
  "Create a new machine instance"
  [type & {:keys [id config]}]
  (let [type-config (get-in @machine-registry [:types type])
        instance-id (or id (str (random-uuid)))
        machine (->Machine instance-id 
                          (merge type-config config)
                          (atom {:energy 0
                                :active false
                                :resources {}}))]
    (swap! machine-registry assoc-in [:instances instance-id] machine)
    machine))

(defn get-machine
  "Get machine instance by ID"
  [id]
  (get-in @machine-registry [:instances id]))

;; Machine error handling
(defn handle-error!
  "Handle machine error"
  [machine error]
  (error/set-error! (:id machine) error)
  (.stop! machine))

;; System initialization
(defn init-machines! []
  (reset! machine-registry {:types {}
                           :instances {}}))