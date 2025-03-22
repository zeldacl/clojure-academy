(ns cn.academy.blocks.block-node.core
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core]
            [cn.academy.blocks.block-node.config :as config]
            [cn.academy.tech-system.energy-system.api :as energy-api]
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
        (config/get-node-property node-type :max-energy)
        #(get-in @(:state this) [:energy])
        #(swap! (:state this) assoc :energy %)))))

(defn create-node
  "Create a wireless node block of the specified type.
   Valid types: :basic, :standard, :advanced"
  [node-type]
  (->WirelessNode node-type
                  {:material :iron
                   :hardness 3.0
                   :resistance 15.0
                   :light-level 0}))