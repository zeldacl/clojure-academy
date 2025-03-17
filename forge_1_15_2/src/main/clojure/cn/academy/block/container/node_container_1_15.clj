(ns cn.academy.block.container.node-container-1-15
  (:require [cn.academy.block.container.node-container :as core])
  (:import [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.inventory.container Container Slot]
           [net.minecraft.item ItemStack]
           [net.minecraft.util IWorldPosCallable]))

(defrecord NodeContainer1_15 [core-container]
  Container
  (canInteractWith [_ player]
    (core/can-interact core-container player))
  
  (moveItemStackTo [this stack slot-start slot-end reverse?]
    (let [success? (atom false)]
      (when (and (not (.isEmpty stack))
                 (or (< slot-start 2) ; Moving to node slots
                     (core/transfer-slot core-container slot-start 
                       (if (< slot-start 2) :to-player :from-player))))
        (reset! success? true))
      @success?)))

(defn create-container [type id inventory player]
  (->NodeContainer1_15 
    (core/create-container 
      (.-tile inventory)
      player)))