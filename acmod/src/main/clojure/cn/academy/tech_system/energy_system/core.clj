(ns cn.academy.tech-system.energy-system.core
  (:require [cn.academy.tech-system.energy-system.registry :as registry]
            [cn.academy.tech-system.energy-system.network.state :as network-state]
            [cn.academy.tech-system.energy-system.network.handler :as handler]
            [cn.academy.tech-system.energy-system.network.optimization :as optimization]
            [cn.academy.tech-system.energy-system.transfer :as transfer]
            [cn.academy.tech-system.energy-system.persistence :as persistence]
            [cn.academy.tech-system.energy-system.analytics :as analytics]
            [mcmod.event :as event]
            [clojure.tools.logging :as log]))

(defn- init-subsystems! []
  ;; Initialize core systems
  (network-state/init!)
  (handler/init!)
  (optimization/init!)
  (analytics/init!)
  
  ;; Initialize registry with node types
  (registry/init!)
  
  ;; Initialize persistence and transfer
  (persistence/init!)
  (transfer/init-network!)
  
  ;; Register event handlers
  (event/register-handler :world-load 
    (fn [world]
      (persistence/load-all! world)))
  
  (event/register-handler :world-save
    (fn [world]
      (persistence/save-all! world)))
  
  (log/info "Energy system initialized"))

(defn init! []
  (try
    (init-subsystems!)
    (catch Exception e
      (log/error e "Failed to initialize energy system"))))