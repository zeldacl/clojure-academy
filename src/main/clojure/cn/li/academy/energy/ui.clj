(ns cn.li.academy.energy.ui
  (:require [forge-clj.ui :refer [defcontainer defguihandler]]
            [forge-clj.util :refer [construct with-prefix get-tile-entity-at]]
            [cn.li.academy.energy.tileentities :refer [tileentity-node]])
  (:import (net.minecraft.entity.player EntityPlayer)
           (net.minecraft.item ItemStack)
           (net.minecraft.inventory Slot)))

(defcontainer container-node
              :player-hotbar? true
              :player-inventory? true
              :slots [[0 38 71] [1 78 71]]
              :player-inventory-layout (into [] (for [x (range 1 4)
                                                      y (range 3)]
                                                  [(+ (* (- 4 x) 9) y) (+ (* y 18) 8) (- 149 (* x 18))]))
              :player-hotbar-layout (mapv #(vector %1 (+ (* %1 18) 8) 153) (range 9)))

(with-prefix container-node-
             (defn transferStackInSlot [^ContainerNode this ^EntityPlayer player slot-index]
               (let [^Slot slot (.get ^java.util.List (.-inventorySlots this) slot-index)]
                 (if (and slot (.getHasStack slot))
                   (let [^ItemStack istack (.getStack slot)
                         ^ItemStack prev (.copy istack)
                         success (if (< slot-index 2)
                                   (.mergeItemStack this istack 2 (.size (.-inventorySlots this)) true)
                                   (.mergeItemStack this istack 0 2 false))]
                     (when success
                       (if (= (.-stackSize istack) 0)
                         (.putStack slot nil)
                         (.onSlotChanged slot))
                       (when (not= (.-stackSize istack) (.-stackSize prev))
                         (.onPickupFromSlot slot player istack)
                         prev))))))
             (defn canInteractWith [^ContainerNode this player]
               (let [node (:bound-inventory @(.data this))]
                 (< (.getDistanceSq (+ (.-xCoord node) 0.5) (+ (.-yCoord node) 0.5) (+ (.-zCoord node) 0.5)) 64))))


(defn get-server-gui [id ^EntityPlayer player world x y z]
  (let [te (get-tile-entity-at world x y z)]
    (if (instance? tileentity-node te)
      (construct container-node (.-inventory player) te))))

(defn get-client-gui [id ^EntityPlayer player world x y z]
  (if-let [c (get-server-gui id player world x y z)]
    true
    ;(construct test-gui-container c)
    ))

(defguihandler block-node-gui-handler
               get-server-gui
               get-client-gui)
