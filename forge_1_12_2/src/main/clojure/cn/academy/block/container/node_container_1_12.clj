(ns cn.academy.block.container.node-container-1-12
  (:require [cn.academy.block.container.node-container :as core])
  (:import [net.minecraft.entity.player EntityPlayer]
           [net.minecraft.inventory Container Slot]
           [net.minecraft.item ItemStack]))

(defrecord NodeContainer1_12 [core-container]
  Container
  (canInteractWith [_ player]
    (core/can-interact core-container player))
  
  (transferStackInSlot [this player slot-id]
    (let [slot (.getSlot this slot-id)
          stack (.getStack slot)]
      (if (.isEmpty stack)
        ItemStack/EMPTY
        (do 
          (core/transfer-slot core-container slot-id
                             (if (< slot-id 2) :to-player :from-player))
          stack)))))

(defn create-container [tile player]
  (->NodeContainer1_12 (core/create-container tile player)))