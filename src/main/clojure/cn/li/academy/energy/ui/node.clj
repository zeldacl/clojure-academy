(ns cn.li.academy.energy.ui.node
  (:require [cn.li.mcmod.ui :refer [defblockcontainer slot-inv]]
            [cn.li.academy.energy.ui.slots :refer [slot-ifitem]]
            [cn.li.academy.ac-blocks :refer [block-node-instance]]
            [cn.li.academy.energy.utils :refer [imag-energy-item? make-transfer-rules make-energy-transfer-stack-in-slot-fn]])
  (:import (net.minecraft.entity.player PlayerEntity)
           (net.minecraft.inventory.container Container)
           (net.minecraft.util IWorldPosCallable)
           (net.minecraft.tileentity TileEntity)))


;(defn transferStackInSlot [^Container container ^PlayerEntity player, ^long index]
;  (let [slot (.get (.-inventorySlots container) index)]))

(defblockcontainer container-node
  :overrides {
              :transferStackInSlot (make-energy-transfer-stack-in-slot-fn
                                     [(make-transfer-rules slot-ifitem slot-inv)
                                      (make-transfer-rules slot-inv slot-ifitem imag-energy-item?)])
              :canInteractWith     (fn [this ^PlayerEntity playerIn]
                                     (let [^container-node this this
                                           ^TileEntity tileentity (:tileentity @(.-data this))]
                                       (Container/isWithinUsableDistance
                                         (IWorldPosCallable/of (.getWorld tileentity) (.getPos tileentity))
                                         playerIn block-node-instance))
                                     )
              })


;(defmethod transfer-slot? [slot-ifitem slot-inv] [from to] true)
;(defmethod transfer-slot? [slot-inv slot-ifitem] [from to] false)     ;todo
