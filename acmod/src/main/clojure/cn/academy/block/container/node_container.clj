(ns cn.academy.block.container.node-container
  (:require [cn.academy.api.container :as container-api]
            [cn.academy.api.item :as item-api])
  (:import [net.minecraft.inventory IInventory]
           [net.minecraft.entity.player EntityPlayer]))

(defprotocol INodeContainer
  (init-slots [this])
  (can-interact [this player])
  (transfer-slot [this slot-id player-action]))

(defrecord NodeContainer [tile player]
  INodeContainer
  (init-slots [this]
    ;; Add slots for energy items
    (doto this
      (container-api/add-slot "INPUT" tile 0 56 17) ; Input slot for charging
      (container-api/add-slot "OUTPUT" tile 1 56 53) ; Output slot for discharging
      (container-api/map-player-inventory 8 84))) ; Player inventory at y=84
  
  (can-interact [_ player]
    (.isWithinUsableDistance player (.getPos tile)))
  
  (transfer-slot [this slot-id action]
    (let [input-slot (container-api/get-slot this 0)
          output-slot (container-api/get-slot this 1)
          player-slots (container-api/get-player-slots this)
          energy-handler (item-api/get-energy-handler input-slot)]
      (case action
        :to-player (container-api/merge-into-player this slot-id player-slots)
        :from-player (when (item-api/is-energy-item? slot-id)
                      (container-api/merge-into-slot this slot-id input-slot))))))

(defn create-container [tile player]
  (let [container (->NodeContainer tile player)]
    (init-slots container)
    container))