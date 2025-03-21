(ns cn.academy.blocks.block-node.core
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core]
            [cn.academy.blocks.block-node.config :as config]
            [cn.academy.tech-system.energy-system.api :as energy-api]
            [cn.academy.tech-system.energy-system.registry :as energy-registry]
            [cn.academy.tech-system.energy-system.network.handler :as network]
            [clojure.tools.logging :as log]))

(defrecord WirelessNode [node-type properties]
  IBlock
  (get-properties [this]
    properties)
    
  (on-placed [this world pos placer]
    (when-let [te (get-tile-entity world pos)]
      (mark-dirty te)))
      
  (on-broken [this world pos]
    (when-let [te (get-tile-entity world pos)]
      (invalidate-caps te)))
      
  IWirelessCapability
  (get-capability [this cap dir]
    (when (instance? IEnergyStorage cap)
      (energy-api/create-energy-storage 
        (energy-registry/get-node-type-property node-type :max-energy)
        #(get-in @(:state this) [:energy])
        #(swap! (:state this) assoc :energy %)))))

(defn create-node [node-type]
  (->WirelessNode node-type
                  {:material :iron
                   :hardness 3.0
                   :resistance 15.0
                   :light-level 0}))

(def create-basic-node #(create-node :basic))
(def create-standard-node #(create-node :standard)) 
(def create-advanced-node #(create-node :advanced))