(ns cn.academy.blocks.block-node.core
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core]
            [clojure.tools.logging :as log]))

(def node-types
  {:basic {:range 8
           :max-connections 4
           :max-energy 5000}
   :standard {:range 16
              :max-connections 8  
              :max-energy 20000}
   :advanced {:range 32
              :max-connections 16
              :max-energy 100000}})

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
      (reify IEnergyStorage
        (receive-energy [_ amount simulate]
          (if-let [te (get-tile-entity (get-world this) (get-position this))]
            (receive-energy te amount simulate)
            0))
        (extract-energy [_ amount simulate]  
          (if-let [te (get-tile-entity (get-world this) (get-position this))]
            (extract-energy te amount simulate)
            0))
        (get-energy-stored [_]
          (if-let [te (get-tile-entity (get-world this) (get-position this))]
            (get-energy te)
            0))
        (get-max-energy-stored [_]
          (get-in node-types [node-type :max-energy]))
        (can-receive? [_] true)
        (can-extract? [_] true))))

  (invalidate-caps [this]
    (when-let [te (get-tile-entity (get-world this) (get-position this))]
      (invalidate-capabilities te))))

(defn create-node [node-type]
  (->WirelessNode node-type
                  {:material :iron
                   :hardness 3.0
                   :resistance 15.0
                   :light-level 0}))

(def create-basic-node #(create-node :basic))
(def create-standard-node #(create-node :standard)) 
(def create-advanced-node #(create-node :advanced))