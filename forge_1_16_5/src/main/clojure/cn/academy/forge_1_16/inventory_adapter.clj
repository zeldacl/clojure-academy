(ns cn.academy.forge-1-16.inventory-adapter
  (:require [cn.academy.inventory.core :as inv])
  (:import [net.minecraftforge.items IItemHandler IItemHandlerModifiable]
           [net.minecraft.item ItemStack]))

(defn- to-forge-stack [stack]
  (if (nil? stack)
    ItemStack/EMPTY
    stack))

(deftype ForgeItemHandler [inventory]
  IItemHandler
  (getSlots [_]
    (inv/get-slots inventory))
  
  (getStackInSlot [_ slot]
    (to-forge-stack (inv/get-stack-in-slot inventory slot)))
  
  (insertItem [_ slot stack simulate]
    (to-forge-stack (inv/insert-item inventory slot stack simulate)))
  
  (extractItem [_ slot amount simulate]
    (to-forge-stack (inv/extract-item inventory slot amount simulate)))
  
  (getSlotLimit [_ slot]
    (inv/get-slot-limit inventory slot))
  
  (isItemValid [_ slot stack]
    (inv/is-item-valid? inventory slot stack))
  
  IItemHandlerModifiable
  (setStackInSlot [_ slot stack]
    (inv/set-stack inventory slot (to-forge-stack stack))))

(defprotocol IInventoryFactory
  (create-inventory [this size] "Create inventory")
  (create-handler [this inventory] "Create item handler adapter"))

(defrecord ForgeInventoryFactory []
  IInventoryFactory
  (create-inventory [_ size]
    (inv/create-inventory size))
  
  (create-handler [_ inventory]
    (->ForgeItemHandler inventory)))

(defn create-factory []
  (->ForgeInventoryFactory))