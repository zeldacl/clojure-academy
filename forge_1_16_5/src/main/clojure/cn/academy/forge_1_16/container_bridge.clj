(ns cn.academy.forge-1-16.container-bridge
  (:import [net.minecraft.inventory.container Container Slot]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.inventory IInventory]))

(defprotocol IContainerProvider
  "Protocol for container lifecycle"
  (create-container [this type player world pos]
    "Create a container instance")
  (create-slots [this container]
    "Create container slots")
  (can-interact [this player pos]
    "Check if player can interact")
  (on-slot-click [this container slot button type player]
    "Handle slot click")
  (transfer-slot [this container slot player]
    "Handle slot transfer"))

(defn create-base-container [provider type player world pos]
  (proxy [Container] [type]
    (stillValid [player]
      (can-interact provider player pos))
    
    (clicked [slot-id button type player]
      (on-slot-click provider this slot-id button type player))
    
    (quickMoveStack [player slot-id]
      (transfer-slot provider this slot-id player))))

(defn add-slot [container inventory index x y]
  (.addSlot container (Slot. inventory index x y)))

(defn add-player-slots [container player]
  (let [inventory (.inventory player)]
    ; Add main inventory slots
    (doseq [row (range 3)
            col (range 9)]
      (add-slot container 
                inventory
                (+ (* row 9) col 9)
                (+ 8 (* col 18))
                (+ 84 (* row 18))))
    ; Add hotbar slots
    (doseq [col (range 9)]
      (add-slot container
                inventory
                col
                (+ 8 (* col 18))
                142))))