(ns cn.academy.tech-system.energy-system.core
  (:require [cn.academy.tech-system.energy-system.registry :as registry]
            [cn.academy.tech-system.energy-system.network.state :as network-state]
            [cn.academy.tech-system.energy-system.network.handler :as handler]
            [cn.academy.tech-system.energy-system.network.optimization :as optimization]
            [cn.academy.tech-system.energy-system.transfer :as transfer]
            [cn.academy.tech-system.energy-system.persistence :as persistence]
            [cn.academy.tech-system.energy-system.analytics :as analytics]
            [cn.academy.tech-system.energy-system.monitoring :as monitoring]
            [cn.academy.tech-system.energy-system.security.manager :as security]
            [cn.academy.tech-system.energy-system.block.adapter :as block-adapter]
            [mcmod.event :as event]
            [clojure.tools.logging :as log]))

(defn- init-network-components! []
  ;; Initialize core network functionality
  (network-state/init!)
  (handler/init!)
  (optimization/init!)
  
  ;; Initialize security and monitoring
  (security/init!)
  (monitoring/start-monitoring!)
  
  ;; Initialize analytics and persistence
  (analytics/init!)
  (persistence/init!))

(defn- init-block-systems! []
  ;; Register block-specific handlers
  (doseq [[msg-type handler-fn] {:block-destroyed handler/handle-message
                                :update-energy handler/handle-message
                                :multiblock-formed handler/handle-message
                                :multiblock-broken handler/handle-message}]
    (handler/register-handler! msg-type handler-fn))
  
  ;; Initialize block adapter system
  (block-adapter/init!))

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

(defn init! []
  (try
    ;; Initialize registry first as other systems depend on it
    (registry/init!)
    
    ;; Initialize all subsystems
    (init-network-components!)
    (init-block-systems!)
    (register-event-handlers!)
    
    (log/info "Energy system initialized successfully")
    true
    (catch Exception e
      (log/error e "Failed to initialize energy system")
      false)))