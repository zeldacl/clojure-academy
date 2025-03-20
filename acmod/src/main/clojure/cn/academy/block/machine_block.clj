(ns cn.academy.block.machine-block
  (:require [mcmod.factory :as factory]
            [mcmod.registry :as registry]
            [mcmod.protocols.energy :as energy]))

(defrecord MachineBlockEnergy [state]
  energy/IEnergyHandler  
  (get-energy [_]
    (:energy @state))
  
  (get-max-energy [_]
    (:max-energy @state))
  
  (receive-energy [this amount]
    (let [max-receive (:max-receive @state)
          actual-receive (min amount max-receive)
          new-amount (min (+ (get-energy this) actual-receive)
                         (get-max-energy this))]
      (swap! state assoc :energy new-amount)
      actual-receive))
  
  (extract-energy [this amount]
    (let [max-extract (:max-extract @state)
          actual-extract (min amount max-extract (get-energy this))]
      (swap! state update :energy - actual-extract)
      actual-extract)))

(def energy-storage-block 
  (factory/create-block
    {:material :metal
     :hardness 3.5
     :resistance 17.5
     :light-level 7
     :has-tile-entity true}))

(defn register! [mod-id]
  (registry/register-block! mod-id "energy_storage" energy-storage-block))