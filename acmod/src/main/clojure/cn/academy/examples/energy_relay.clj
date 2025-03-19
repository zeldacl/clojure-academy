(ns cn.academy.examples.energy-relay
  (:require [mcmod.protocols :refer :all]
            [mcmod.capabilities :as cap]
            [cn.academy.api.public :as api])
  (:import [net.minecraft.block.material Material]))

(defrecord EnergyRelay []
  IBlock
  (get-properties [this]
    {:material Material/IRON
     :hardness 3.0
     :resistance 15.0
     :light-level 4})
  
  (get-material [this]
    Material/IRON)
  
  (get-hardness [this]
    3.0)
  
  (get-resistance [this]
    15.0)
  
  (get-light-level [this]
    4)
  
  cap/ICapabilityProvider
  (has-capability? [this capability-type side]
    (= (.getName capability-type) "forge:energy"))
  
  (get-capability [this capability-type side]
    (when (has-capability? this capability-type side)
      (api/create-energy-storage
        :capacity 50000
        :max-receive 500
        :max-extract 500))))