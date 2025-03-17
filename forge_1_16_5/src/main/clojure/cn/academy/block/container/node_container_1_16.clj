(ns cn.academy.block.container.node-container-1-16
  (:require [cn.academy.block.container.node-container :as core])
  (:import [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.inventory.container Container Slot]
           [net.minecraft.item ItemStack]
           [net.minecraft.util IWorldPosCallable]
           [net.minecraft.inventory.container ContainerType]))

(defrecord NodeContainer1_16 [core-container]
  Container
  (stillValid [_ player]
    (core/can-interact core-container player))
  
  (quickMoveStack [this player slot-id]
    (let [slot (.getSlot this slot-id)
          stack (.getItem_ slot)]
      (if (.isEmpty stack)
        ItemStack/EMPTY
        (do
          (core/transfer-slot core-container slot-id
                           (if (< slot-id 2) :to-player :from-player))
          stack)))))

(defn create-container [type id inventory player]
  (->NodeContainer1_16
    (core/create-container 
      (.-tile inventory)
      player)))