(ns forge-impl.gui-wrapper
  (:require [mcmod.gui :as gui])
  (:import [net.minecraft.inventory container Container]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.item ItemStack]))

(defn create-container-wrapper [mcmod-container player inventory]
  (proxy [Container] [nil -1]
    (transferStackInSlot [^PlayerEntity player ^Integer index]
      (gui/transfer-stack-in-slot mcmod-container player index)
      ItemStack/EMPTY)
    
    (canInteractWith [^PlayerEntity player]
      (gui/can-interact-with mcmod-container player))
    
    (getInventory []
      inventory)))