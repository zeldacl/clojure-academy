(ns cn.academy.forge.inventory-adapter
  (:require [cn.academy.inventory.core :as inv])
  (:import [net.minecraft.inventory IInventory]
           [net.minecraftforge.items IItemHandler]
           [net.minecraft.item ItemStack]
           [net.minecraft.nbt CompoundNBT]))

(defn- wrap-inventory [inventory]
  (reify IInventory
    (getSizeInventory [_]
      (inv/get-size inventory))
    
    (isEmpty [_]
      (inv/is-empty? inventory))
    
    (getStackInSlot [_ slot]
      (or (inv/get-stack inventory slot)
          ItemStack/EMPTY))
    
    (removeStackFromSlot [_ slot]
      (let [stack (inv/get-stack inventory slot)]
        (inv/set-stack inventory slot nil)
        (or stack ItemStack/EMPTY)))
    
    (setInventorySlotContents [_ slot stack]
      (inv/set-stack inventory slot stack))
    
    (isItemValidForSlot [_ slot stack]
      (inv/is-valid-stack? inventory slot stack))))

(defn- wrap-item-handler [inventory]
  (reify IItemHandler
    (getSlots [_]
      (inv/get-slots inventory))
    
    (getStackInSlot [_ slot]
      (or (inv/get-stack-in-slot inventory slot)
          ItemStack/EMPTY))
    
    (insertItem [_ slot stack simulate]
      (inv/insert-item inventory slot stack simulate))
    
    (extractItem [_ slot amount simulate]
      (inv/extract-item inventory slot amount simulate))
    
    (getSlotLimit [_ slot]
      (inv/get-slot-limit inventory slot))
    
    (isItemValid [_ slot stack]
      (inv/is-item-valid? inventory slot stack))))

(defn create-forge-adapter 
  "Creates a Forge-compatible inventory adapter for the given inventory instance"
  [inventory]
  {:inventory (wrap-inventory inventory)
   :item-handler (wrap-item-handler inventory)})

(defn save-to-nbt [inventory]
  (let [nbt (CompoundNBT.)]
    (doseq [[k v] (inv/save-to-nbt inventory)]
      (.putString nbt k v))
    nbt))

(defn load-from-nbt [factory nbt]
  (let [data (reduce (fn [m k]
                      (assoc m k (.getString nbt k)))
                    {}
                    (iterator-seq (.keySet nbt)))]
    (inv/load-from-nbt factory data)))