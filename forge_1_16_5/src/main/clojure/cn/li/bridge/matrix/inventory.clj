(ns cn.li.bridge.matrix.inventory
  (:require [cn.li.bridge.matrix.api :as matrix]
            [cn.li.bridge.inventory.api :as inventory])
  (:import [net.minecraftforge.items IItemHandler IItemHandlerModifiable]
           [net.minecraft.item ItemStack]
           [net.minecraft.nbt CompoundNBT]))

(def ^:const CORE_SLOT 0)
(def ^:const FIRST_PLATE_SLOT 1)
(def ^:const PLATE_SLOTS 8)
(def ^:const TOTAL_SLOTS (inc PLATE_SLOTS))

(defrecord MatrixInventoryHandler [matrix]
  IItemHandler
  (getSlots [_]
    TOTAL_SLOTS)
  
  (getStackInSlot [_ slot]
    (if (= slot CORE_SLOT)
      (get-in @(.state-atom matrix) [:core])
      (get-in @(.state-atom matrix) [:plates (dec slot)])))
  
  (insertItem [this slot stack simulate]
    (let [current (.getStackInSlot this slot)]
      (if (or (.isEmpty stack)
              (not (matrix/is-valid-item? matrix slot stack)))
        stack ; Return unchanged stack if invalid
        (let [insert-amount (min (.getCount stack)
                               (- (.getMaxStackSize stack)
                                  (if (.isEmpty current) 0 (.getCount current))))]
          (if (zero? insert-amount)
            stack ; No space available
            (let [remaining-stack (.copy stack)]
              (if (not simulate)
                (if (= slot CORE_SLOT)
                  (swap! (.state-atom matrix) assoc :core stack)
                  (swap! (.state-atom matrix) update-in [:plates (dec slot)] 
                         (fn [_] (.copy stack)))))
              (.shrink remaining-stack insert-amount)
              remaining-stack))))))
  
  (extractItem [this slot amount simulate]
    (let [current (.getStackInSlot this slot)]
      (if (or (.isEmpty current) (<= amount 0))
        ItemStack/EMPTY
        (let [extract-amount (min amount (.getCount current))
              extracted-stack (.copy current)]
          (.setCount extracted-stack extract-amount)
          (when (not simulate)
            (if (= slot CORE_SLOT)
              (swap! (.state-atom matrix) assoc :core ItemStack/EMPTY)
              (swap! (.state-atom matrix) assoc-in [:plates (dec slot)] ItemStack/EMPTY)))
          extracted-stack))))
  
  inventory/IInventory
  (get-size [_] 
    TOTAL_SLOTS)
  
  (get-stack [this slot]
    (.getStackInSlot this slot))
  
  (set-stack [this slot stack]
    (.insertItem this slot stack false))
  
  (remove-stack [this slot amount]
    (.extractItem this slot amount false))
  
  (is-empty? [_]
    (every? #(.isEmpty (.getStackInSlot this %))
            (range TOTAL_SLOTS))))

(defn create-inventory-handler [matrix]
  (->MatrixInventoryHandler matrix))