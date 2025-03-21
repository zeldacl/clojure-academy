(ns cn.academy.tech-system.energy-system.registry
  (:require [cn.academy.tech-system.energy-system.network.optimization :as optimization]
            [cn.academy.tech-system.energy-system.transfer :as transfer]
            [cn.academy.tech-system.energy-system.network.handler :as handler]
            [mcmod.registry :as registry]
            [clojure.tools.logging :as log]))

;; Store registered node types
(def ^:private node-types-registry (atom {}))

(defn register-node-type! 
  "Register a node type configuration with the energy system"
  [node-type config]
  (swap! node-types-registry assoc node-type config)
  (log/info "Registered node type:" node-type))

(defn get-node-type-config 
  "Get configuration for a registered node type"
  [node-type]
  (get @node-types-registry node-type))

(defn get-node-type-property
  "Get a specific property from a node type's configuration"
  [node-type property]
  (get-in @node-types-registry [node-type property]))

(defn init! []
  ;; Initialize network components
  (optimization/init!)
  (transfer/init!)
  (handler/init!)
  
  (log/info "Energy system registry initialized with"
            (count @node-types-registry) "node types"))