(ns cn.academy.tech-system.energy-system.core
  (:require [cn.academy.tech-system.energy-system.registry :as registry]
            [cn.academy.tech-system.energy-system.network.state :as network-state]
            [cn.academy.tech-system.energy-system.network.handler :as handler]
            [cn.academy.tech-system.energy-system.network.optimization :as optimization]
            [cn.academy.tech-system.energy-system.network.core :as network-core]
            [cn.academy.tech-system.energy-system.transfer :as transfer]
            [cn.academy.tech-system.energy-system.config :as config]
            [cn.academy.tech-system.energy-system.persistence :as persistence]
            [cn.academy.tech-system.energy-system.analytics :as analytics]
            [cn.academy.tech-system.energy-system.monitoring :as monitoring]
            [cn.academy.tech-system.energy-system.security :as security]
            [cn.academy.tech-system.energy-system.block.adapter :as block-adapter]
            [cn.academy.tech-system.energy-system.admin :as admin]
            [mcmod.event :as event]
            [clojure.tools.logging :as log]))

(def ^:private initialization-state (atom {:initialized false
                                          :startup-time nil
                                          :subsystems {}}))

(defn- mark-subsystem-initialized! [subsystem-id success?]
  (swap! initialization-state assoc-in [:subsystems subsystem-id] 
         {:initialized success?
          :timestamp (System/currentTimeMillis)}))

(defn- init-subsystem! [subsystem-id init-fn]
  (try
    (log/info "Initializing subsystem:" subsystem-id)
    (let [result (init-fn)]
      (mark-subsystem-initialized! subsystem-id true)
      (log/info "Subsystem" subsystem-id "initialized successfully")
      true)
    (catch Exception e
      (mark-subsystem-initialized! subsystem-id false)
      (log/error e "Failed to initialize subsystem:" subsystem-id)
      false)))

(defn- init-config! []
  (init-subsystem! :config
    (fn []
      (config/load-config!)
      true)))

(defn- init-registry! []
  (init-subsystem! :registry
    (fn [] 
      (registry/init!)
      true)))

(defn- init-network-components! []
  ;; Initialize core network functionality
  (init-subsystem! :network-state #(network-state/init!))
  (init-subsystem! :network-handler #(handler/init!))
  (init-subsystem! :network-optimization #(optimization/init!))
  (init-subsystem! :network-core #(network-core/init!)))

(defn- init-security! []
  (init-subsystem! :security #(security/init!)))

(defn- init-monitoring! []
  (init-subsystem! :monitoring #(monitoring/start-monitoring!)))

(defn- init-analytics! []
  (init-subsystem! :analytics #(analytics/init!)))

(defn- init-persistence! []
  (init-subsystem! :persistence #(persistence/init!)))

(defn- init-block-systems! []
  ;; Register block-specific handlers
  (init-subsystem! :block-adapter
    (fn []
      ;; Register block-specific handlers
      (doseq [[msg-type handler-fn] {:block-destroyed handler/handle-message
                                    :update-energy handler/handle-message
                                    :multiblock-formed handler/handle-message
                                    :multiblock-broken handler/handle-message}]
        (handler/register-handler! msg-type handler-fn))
      
      ;; Initialize block adapter system
      (block-adapter/init!))))

(defn- register-event-handlers! []
  ;; World load/save handlers
  (event/register-handler :world-load 
    (fn [world]
      (persistence/load-all! world)))
  
  (event/register-handler :world-save
    (fn [world]
      (persistence/save-all! world)))
  
  ;; Block event handlers
  (event/register-handler :block-added
    (fn [block]
      (when-let [node (block-adapter/from-block block)]
        (network-state/register-node! node))))
  
  (event/register-handler :block-removed
    (fn [block]
      (when-let [node (block-adapter/from-block block)]
        (network-state/remove-node! (:id node))))))

(defn- init-admin! []
  (init-subsystem! :admin
    (fn []
      ;; Register admin commands
      (admin/register-commands!)
      true)))

(defn init! []
  (when-not (:initialized @initialization-state)
    (try
      (log/info "Initializing energy system...")
      (swap! initialization-state assoc :startup-time (System/currentTimeMillis))
      
      ;; Initialization order matters here - dependencies between subsystems
      
      ;; First load configuration
      (init-config!)
      
      ;; Then initialize registry as other systems depend on it
      (init-registry!)
      
      ;; Initialize networking components
      (init-network-components!)
      
      ;; Initialize security first as other components may use it
      (init-security!)
      
      ;; Initialize analytics before monitoring (dependency)
      (init-analytics!)
      
      ;; Initialize monitoring
      (init-monitoring!)
      
      ;; Initialize persistence after other stateful components
      (init-persistence!) 
      
      ;; Initialize block-related systems
      (init-block-systems!)
      
      ;; Register event handlers
      (register-event-handlers!)
      
      ;; Initialize admin interface last
      (init-admin!)
      
      ;; Mark system as initialized
      (swap! initialization-state assoc :initialized true)
      (let [startup-time (- (System/currentTimeMillis) (:startup-time @initialization-state))]
        (log/info "Energy system initialized successfully in" startup-time "ms"))
      true
      (catch Exception e
        (log/error e "Failed to initialize energy system")
        false))))

(defn get-initialization-status []
  {:initialized? (:initialized @initialization-state)
   :subsystems (:subsystems @initialization-state)
   :startup-time (when-let [start (:startup-time @initialization-state)]
                   (- (System/currentTimeMillis) start))})