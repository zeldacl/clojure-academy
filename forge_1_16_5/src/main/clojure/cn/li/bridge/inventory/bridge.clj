(ns cn.li.bridge.inventory.bridge
  (:require [cn.li.bridge.inventory.core :as inv]
            [mcmod.protocols :refer [IInventoryBridge]])
  (:import [net.minecraft.inventory IInventory]
           [net.minecraft.item ItemStack]
           [net.minecraftforge.items IItemHandler IItemHandlerModifiable]))

(defrecord ForgeInventoryWrapper [forge-inv]
  inv/IInventory
  (get-size [_]
    (.getContainerSize forge-inv))
  
  (get-stack [_ slot]
    (let [stack (.getItem forge-inv slot)]
      {:item (.getItem stack)
       :count (.getCount stack)}))
  
  (set-stack [_ slot stack]
    (.setItem forge-inv slot
              (if stack
                (ItemStack. (:item stack) (:count stack))
                ItemStack/EMPTY)))
  
  (remove-stack [_ slot amount]
    (let [stack (.removeItem forge-inv slot amount)]
      {:item (.getItem stack)
       :count (.getCount stack)}))
  
  (is-empty? [_]
    (.isEmpty forge-inv))
  
  (mark-dirty [_]
    (.setChanged forge-inv)))

(defrecord ForgeItemHandlerWrapper [forge-handler]
  inv/IItemHandler
  (get-slots [_]
    (.getSlots forge-handler))
  
  (get-stack-in-slot [_ slot]
    (let [stack (.getStackInSlot forge-handler slot)]
      {:item (.getItem stack)
       :count (.getCount stack)}))
  
  (insert-item [_ slot stack simulate]
    (let [result (.insertItem forge-handler slot
                             (ItemStack. (:item stack) (:count stack))
                             simulate)]
      {:item (.getItem result)
       :count (.getCount result)}))
  
  (extract-item [_ slot amount simulate]
    (let [result (.extractItem forge-handler slot amount simulate)]
      {:item (.getItem result)
       :count (.getCount result)}))