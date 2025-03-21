(ns cn.academy.tech-system.energy-system.registry
  (:require [cn.academy.tech-system.energy-system.network.wireless :as wireless]
            [cn.academy.tech-system.energy-system.capability.wireless :as wireless-cap]
            [cn.academy.tech-system.energy-system.api :as energy-api]
            [mcmod.registry :as registry]
            [clojure.tools.logging :as log]))

(def ^:private node-types (atom {}))

(defn register-node-type!
  "Register a new node type with default properties"
  [id props]
  (swap! node-types assoc id props))

(defn get-node-type
  "Get node type properties"
  [id]
  (get @node-types id))

(defn create-node-capability
  "Create a wireless node capability with type-specific properties"
  [node-type]
  (when-let [props (get-node-type node-type)]
    (wireless-cap/create-capability
      (:max-energy props)
      (:range props)
      (:max-connections props))))

;; Initialize default node types
(defn init! []
  ;; Register basic node type
  (register-node-type! :basic
    {:max-energy 5000
     :range 8
     :max-connections 4
     :bandwidth 500})
  
  ;; Register standard node type  
  (register-node-type! :standard
    {:max-energy 20000
     :range 16
     :max-connections 8
     :bandwidth 1000})
  
  ;; Register advanced node type
  (register-node-type! :advanced
    {:max-energy 100000
     :range 32
     :max-connections 16
     :bandwidth 2000})
  
  ;; Set up energy implementation
  (energy-api/set-energy-impl! 
    {:create-network wireless/create-network!
     :get-network wireless/get-network
     :get-node-network wireless/get-node-network
     :create-node-capability create-node-capability}))