(ns forge-impl.container-impl
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.inventory container Container Slot]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.item ItemStack]))

(defrecord ForgeInventorySlot [^Slot slot]
  IInventorySlot
  (get-stack [_]
    (.getStack slot))
  
  (set-stack [_ stack]
    (.putStack slot stack))
  
  (get-max-stack-size [_]
    (.getSlotStackLimit slot))
  
  (is-item-valid? [_ stack]
    (.isItemValid slot stack)))

(defrecord ForgeContainer [^Container container]
  IContainer
  (get-slots [_]
    (.inventorySlots container))
  
  (get-slot [_ idx]
    (.getSlot container idx))
  
  (set-slot [_ idx stack]
    (.putStackInSlot container idx stack))
  
  (can-interact-with [_ player]
    (.canInteractWith container player))
  
  (transfer-stack-in-slot [_ player slot-id]
    (.transferStackInSlot container player slot-id))
  
  (detect-changes [_]
    (.detectAndSendChanges container))
  
  (on-closed [_ player]
    (.onContainerClosed container player))

  (get-inventory [_]
    (.getInventory container))
  
  (transfer-stack [_ player index]
    (.transferStackInSlot container player index))
  
  (merge-stack [_ slot stack]
    (.mergeItemStack container stack 
                     (.slotNumber slot) 
                     (inc (.slotNumber slot)) 
                     false))
  
  (detect-sync-changes [_]
    (.detectAndSendChanges container)))

(defrecord ForgeInventory [inventory]
  IInventory
  (get-size [_]
    (.getSizeInventory inventory))
  
  (get-stack-in-slot [_ slot]
    (.getStackInSlot inventory slot))
  
  (remove-stack-in-slot [_ slot amount]
    (.decrStackSize inventory slot amount))
  
  (set-inventory-slot [_ slot stack]
    (.setInventorySlotContents inventory slot stack))
  
  (is-empty? [_]
    (.isEmpty inventory))
  
  (mark-dirty [_]
    (.markDirty inventory)))