(ns cn.academy.block.processor-block
  (:require [mcmod.factory :as factory]
            [mcmod.registry :as registry]))

(def processor-inventory 
  (factory/create-inventory 2)) ; Input and output slots

(def processor-block
  (factory/create-block
    {:material :metal
     :hardness 4.0
     :resistance 20.0
     :light-level 5
     :has-tile-entity true
     :on-activated (fn [pos data]
                    (let [{:keys [world player]} data]
                      ; Open GUI in the future
                      true))
     :render-type :cutout ; Show model with transparent parts
     :opaque? false}))

(def processor-te
  (factory/create-tile-entity
    {:inventory processor-inventory
     :processing-time 100 ; Ticks to process
     :on-tick (fn [state]
                (let [inv (:inventory @state)]
                  (when-let [input (get-stack-in-slot inv 0)]
                    ; Process input -> output logic here
                    (swap! state update :progress inc)
                    (when (>= (:progress @state) (:processing-time @state))
                      ; Complete processing
                      (remove-stack-in-slot inv 0 1)
                      (set-inventory-slot inv 1 {:id "processed_item" :count 1})
                      (swap! state assoc :progress 0)))))
     :on-load (fn [state]
                (reset! state {:progress 0
                             :inventory processor-inventory}))
     :get-update (fn [state]
                  {:progress (:progress @state)})
     :handle-update (fn [state packet]
                     (swap! state assoc :progress (:progress packet)))}))

(defn register! [mod-id]
  ; Register block and tile entity
  (registry/register-block! mod-id "item_processor" processor-block)
  (registry/register-tile-entity! mod-id "item_processor" 
                                "item_processor"
                                processor-te))