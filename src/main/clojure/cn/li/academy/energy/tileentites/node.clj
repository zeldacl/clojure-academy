(ns cn.li.academy.energy.tileentites.node
  (:require [cn.li.mcmod.tileentity :refer [deftilerntity]]
            [cn.li.mcmod.blocks :refer [get-block-states]]
            [cn.li.mcmod.utils :refer [->LazyOptional]]
            [cn.li.academy.energy.utils :refer [imag-energy-item?]]
            [cn.li.academy.energy.common :refer [get-node-attr]])
  (:import (net.minecraft.entity.player PlayerEntity)
           (net.minecraftforge.items ItemStackHandler)
           (net.minecraft.tileentity TileEntity)
           (cn.li.academy.api.energy.capability WirelessNode)
           (net.minecraft.block BlockState)
           (cn.li.academy.api.energy ImagEnergyItem)
           (net.minecraftforge.common.util LazyOptional)))


(defn create-slots [tile size]
  (proxy [ItemStackHandler] [size]
    (onContentsChanged [slot]
      (.markDirty ^TileEntity tile))
    (isItemValid [slot, stack]
      (imag-energy-item? stack))))

(defn create-wireless-node [tile]
  (let [node-attr-fn (fn [attr-key]
                       (let [block-state (.getBlockState tile)
                             node-type-id (.get ^BlockState block-state (get-block-states :node-type))]
                         (get-node-attr node-type-id attr-key)))]
    (proxy [WirelessNode] []
           (getMaxEnergy [] (node-attr-fn :max-energy))
           (getBandwidth [] (node-attr-fn :band-width))
           (getCapacity [] (node-attr-fn :range))
           (getRange [] (node-attr-fn :capacity)))))

(defn update-charge-in [^ItemStackHandler slot ^WirelessNode wireless-node]
  (let [stack (.getStackInSlot slot 0)]
    (when (imag-energy-item? stack)
      (let [item ^ImagEnergyItem (.getItem stack)
            bandwidth (min (.getBandwidth wireless-node) (.getBandwidth item))
            trans-energy (min bandwidth
                           (.getEnergy item)
                           (- (.getMaxEnergy wireless-node) (.getEnergy wireless-node)))]
        (.setEnergy item (- (.getEnergy item) trans-energy ))
        (.setEnergy wireless-node (+ trans-energy (.getEnergy wireless-node)))))))

(deftilerntity tile-node
  :fields {
           :wireless-node nil
           :slots nil
           }
  :post-init (fn [this &args]
               (assoc! this :slots (->LazyOptional (constantly (create-slots this 2)) ) )
               (assoc! this :wireless-node (->LazyOptional (constantly (create-wireless-node this)) ) ))
  :overrides {
              :tick (fn [this]
                      (let [slots (.orElse ^LazyOptional (:slots this) nil)
                            wireless-node (.orElse ^LazyOptional (:wireless-node this) nil)]
                        (when (and slots wireless-node)
                          (update-charge-in slot-in wireless-node)))
                      (when [])
                      (update-charge-in (:slot-in this) (:wireless-node this))
                      (let [world (.-world this)])
                      )
              })

(defn set-placer [tile-entity placer]
  (when (instance? PlayerEntity placer) nil)
  (throw "err"))
