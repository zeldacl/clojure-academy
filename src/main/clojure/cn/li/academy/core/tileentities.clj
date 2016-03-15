(ns cn.li.academy.core.tileentities
  (:require [forge-clj.tileentity :refer [deftileentity]]
            [forge-clj.util :refer [with-prefix]])
  (:import (net.minecraft.inventory IInventory)
           (net.minecraft.item ItemStack)))

(defn tile-entity-decr-stack-size [^IInventory entity slot count]
  (let [stack (.getStackInSlot entity slot)]
    (if (nil? stack)
      (if (> (.-stackSize stack) count)
        (let [stack1 (.splitStack stack count)]
          (.markDirty entity)
          stack1)
        (do
          (.setInventorySlotContents entity slot nil)
          stack))
      stack)))

(defn tile-entity-get-stack-in-slot-on-closing [^IInventory entity slot]
  (let [stack (.getStackInSlot entity slot)]
    (if stack
      (.setInventorySlotContents entity slot nil))
    stack))


(defmacro deftileinventory [name inv-name size & args]
  (let [classdata (apply hash-map args)
        prefix (str name "-")
        classdata (assoc-in classdata [:fields :inv] (into [] (repeat size nil)))
        classdata (assoc-in classdata [:fields :inv-name] inv-name)
        classdata (assoc classdata :interfaces (conj (get classdata :interfaces []) `IInventory))
        classdata (reduce concat [] (into [] classdata))]
    `(do
       (deftileentity ~name ~@classdata)
       (with-prefix ~prefix
                    (def ~'getSizeInventory (constantly ~size))
                    (defn ~'getStackInSlot [~'this ~'slot]
                      (get (:inv ~'this) ~'slot))
                    (def ~'decrStackSize ~tile-entity-decr-stack-size)
                    (def ~'getStackInSlotOnClosing ~tile-entity-get-stack-in-slot-on-closing)
                    (defn ~'setInventorySlotContents [~'this ~'slot ^ItemStack ~'stack]
                      (assoc! ~'this :inv (assoc (:inv ~'this) ~'slot ~'stack))
                      (if (and ~'stack (> (.-stackSize ~'stack) (.getInventoryStackLimit ~'this)))
                        (set! (.-stackSize ~'stack) (.getInventoryStackLimit ~'this)))
                      (.markDirty ~'this))
                    (def ~'getInventoryName (constantly ~inv-name))
                    (def ~'hasCustomInventoryName (constantly false))
                    (def ~'getInventoryStackLimit (constantly 64))
                    (def ~'isUseableByPlayer (constantly true))
                    (def ~'openInventory (constantly nil))
                    (def ~'closeInventory (constantly nil))
                    (def ~'isItemValidForSlot (constantly true))))))

