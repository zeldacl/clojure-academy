(ns cn.academy.tech-system.energy-system.init
  (:require [cn.academy.tech-system.energy-system.registry :as registry]
            [cn.academy.tech-system.energy-system.persistence :as persistence]
            [cn.academy.tech-system.energy-system.client.handler :as client-handler]
            [cn.academy.tech-system.energy-system.network.wireless :as network]
            [mcmod.event :as event]
            [clojure.tools.logging :as log]))

(defn- init-network-updater! []
  (event/register-handler :server-tick 
    (fn [_]
      (network/update-networks!))))

(defn init! []
  (log/info "Initializing Energy System...")
  
  ;; Initialize core components
  (registry/init!)
  (persistence/init!)
  
  ;; Initialize network updater
  (init-network-updater!)
  
  ;; Initialize client-side handlers when on client
  (when (mcmod.core/is-client?)
    (client-handler/register-handlers!))
  
  (log/info "Energy System initialized successfully"))