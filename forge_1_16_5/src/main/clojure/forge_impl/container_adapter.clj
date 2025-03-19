(ns forge-impl.container-adapter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.inventory.container Container ContainerType]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.item ItemStack]
           [net.minecraft.inventory IInventory]
           [net.minecraft.util Direction]))

(defn create-container-type [mcmod-container]
  (ContainerType/create
    (fn [window-id, player-inv]
      (proxy [Container] [nil window-id]
        (canInteractWith [^PlayerEntity player]
          (can-interact-with mcmod-container player))
        
        (transferStackInSlot [^PlayerEntity player ^Integer index]
          (transfer-stack-in-slot mcmod-container player index)
          ItemStack/EMPTY)
        
        (onContainerClosed [^PlayerEntity player]
          (on-closed mcmod-container player))
        
        (detectAndSendChanges []
          (mark-dirty mcmod-container))
        
        (getInventory []
          (get-slots mcmod-container)))))))