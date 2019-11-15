(ns cn.li.academy.energy.utils
  (:require [cn.li.mcmod.ui :refer [slot-inv]])
  (:import (cn.li.academy.api.energy ImagEnergyItem)
           (net.minecraft.inventory.container Container Slot)
           (net.minecraft.entity.player PlayerEntity)
           (net.minecraft.item Item ItemStack)))

(defn imag-energy-item? [obj]
  (let [item (condp = (class obj)
               Item obj
               ItemStack (.getStack obj)
               nil)]
    (if item (instance? ImagEnergyItem item) false)))


;(def *transfer-rules* (atom []))

(defn make-transfer-rules
  ([from to test] {:from from :to [to] :test test})
  ([from to]
   (make-transfer-rules from to (constantly true))))

(defn make-energy-transfer-stack-in-slot-fn
  ([] (make-energy-transfer-stack-in-slot-fn []))
  ([rules]
   (fn [^Container container ^PlayerEntity player, ^long index]
     (let [^Slot slot (.get (.-inventorySlots container) index)
           stack (if (.getHasStack slot) (.getStack slot) ItemStack/EMPTY)
           rules (filter #(and (instance? (:from %1) slot) ((:test %1) stack)) rules)
           merge (fn [rule]
                   (let [old-stack (.copy stack)
                         to-slot-type? (fn [slot] (some #(instance? %1 slot) (:to rule)))
                         reverse-direction (instance? slot-inv slot)
                         to-slots (mapv #(.slotNumber %1) (filter #(to-slot-type? %1) (.-inventorySlots container)))
                         min (min to-slots)
                         max (max to-slots)]
                     (if (.mergeItemStack ^Container container stack min max reverse-direction)
                       (do
                         (if (.isEmpty stack) (.putStack slot ItemStack/EMPTY) (.onSlotChanged slot))
                         (if (= (.getCount stack) (.getCount old-stack))
                           ItemStack/EMPTY
                           (do (.onTake slot player stack) old-stack))
                         )
                       ItemStack/EMPTY)

                     ))]
       (if (empty? rules)
         stack
         ; only handle first rule
         (merge (first rules)))))))