(ns cn.academy.block.machine-block
  (:require [mcmod.factory :as factory]
            [mcmod.registry :as registry]))

(def energy-storage-block 
  (factory/create-block
    {:material :metal
     :hardness 3.5
     :resistance 17.5
     :light-level 7
     :has-tile-entity true
     :on-activated (fn [pos data]
                    (let [{:keys [world player]} data]
                      ; Interact with player
                      true))
     :on-placed (fn [pos data]
                 (let [{:keys [world player]} data]
                   ; Initialize tile entity
                   nil))
     :on-removed (fn [pos]
                  ; Cleanup
                  nil)}))

(def energy-storage-te
  (factory/create-tile-entity
    {:capabilities {:energy {:storage 10000
                            :max-receive 100
                            :max-extract 100}}
     :on-tick (fn [state]
                (swap! state update :energy #(min (+ % 10) 10000)))
     :on-load (fn [state]
                (reset! state {:energy 0}))
     :get-update (fn [state]
                  {:energy (:energy @state)})
     :handle-update (fn [state packet]
                     (swap! state assoc :energy (:energy packet)))}))

(defn register! [mod-id]
  ; Register block
  (registry/register-block! mod-id "energy_storage" energy-storage-block)
  
  ; Register tile entity
  (registry/register-tile-entity! mod-id "energy_storage" 
                                "energy_storage" 
                                energy-storage-te))