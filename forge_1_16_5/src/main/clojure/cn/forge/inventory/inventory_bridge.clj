(ns cn.forge.inventory.inventory-bridge
  (:require [cn.academy.block.inventory-adapter :as inv])
  (:import [net.minecraftforge.items IItemHandler]
           [net.minecraft.item ItemStack]))

(defn create-forge-inventory-bridge
  "Creates a Forge IItemHandler implementation that delegates to our generic inventory adapter"
  [inventory-adapter]
  (reify IItemHandler
    (getSlots [_]
      (inv/get-size inventory-adapter))
    
    (getStackInSlot [_ slot]
      (or (inv/get-item inventory-adapter slot)
          (ItemStack/EMPTY)))
    
    (insertItem [this slot item simulate]
      (if (and (not (.isEmpty item))
               (inv/validate-item inventory-adapter slot item))
        (let [current (inv/get-item inventory-adapter slot)]
          (if simulate
            item
            (do
              (inv/set-item! inventory-adapter slot item)
              (or current (ItemStack/EMPTY)))))
        item))
    
    (extractItem [this slot amount simulate]
      (let [current (inv/get-item inventory-adapter slot)]
        (if (and current (pos? amount))
          (let [to-extract (min amount (.getCount current))]
            (if simulate
              (.copy current)
              (do
                (inv/set-item! inventory-adapter slot nil)
                current)))
          (ItemStack/EMPTY))))
    
    (getSlotLimit [_ slot]
      (inv/get-max-stack-size inventory-adapter))
    
    (isItemValid [_ slot item]
      (inv/validate-item inventory-adapter slot item))))