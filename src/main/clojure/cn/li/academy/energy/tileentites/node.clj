(ns cn.li.academy.energy.tileentites.node
  (:require [cn.li.mcmod.tileentity :refer [deftilerntity]]
            [cn.li.mcmod.blocks :refer [get-block-states]]
            [cn.li.academy.energy.ui.node :refer [container-node]]
            [cn.li.mcmod.utils :refer [->LazyOptional construct]]
            [cn.li.academy.energy.utils :refer [imag-energy-item?]]
            [cn.li.academy.energy.common :refer [get-node-attr]])
  (:import (net.minecraft.entity.player PlayerEntity PlayerInventory)
           (net.minecraftforge.items ItemStackHandler)
           (net.minecraft.tileentity TileEntity)
           (cn.li.academy.api.energy.capability WirelessNode)
           (net.minecraft.block BlockState)
           (cn.li.academy.api.energy ImagEnergyItem)
           (net.minecraftforge.common.util LazyOptional)
           (net.minecraft.item ItemStack)
           (net.minecraft.world World)
           (net.minecraft.util.math BlockPos)))


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


(defn update-charge-in [^ItemStack stack ^WirelessNode wireless-node]
  (when (imag-energy-item? stack)
    (let [item ^ImagEnergyItem (.getItem stack)
          bandwidth (min (.getBandwidth wireless-node) (.getBandwidth item))
          trans-energy (min bandwidth
                         (.getEnergy item)
                         (- (.getMaxEnergy wireless-node) (.getEnergy wireless-node)))]
      (when (> trans-energy 0)
        (.setEnergy item (- (.getEnergy item) trans-energy ))
        (.setEnergy wireless-node (+ trans-energy (.getEnergy wireless-node)))))))

(defn update-charge-out [^ItemStack stack ^WirelessNode wireless-node]
  (let [energy (.getEnergy wireless-node)]
    (when (and (imag-energy-item? stack) (> energy 0))
      (let [item ^ImagEnergyItem (.getItem stack)
            bandwidth (min (.getBandwidth wireless-node) (.getBandwidth item))
            trans-energy (min bandwidth
                           (.getEnergy wireless-node)
                           (- (.getMaxEnergy item) (.getEnergy item)))]
        (when (> trans-energy 0)
          (.setEnergy wireless-node (- (.getEnergy wireless-node) trans-energy ))
          (.setEnergy item (+ trans-energy (.getEnergy item))))))))

(defn rebuild-block-state [^World world ^BlockPos pos ^WirelessNode wireless-node]
  (let [block-state ^BlockState (.getBlockState world pos)
        block (.getBlock block-state)
        connected (.get block-state (get-block-states :connected))
        energy (.get block-state (get-block-states :energy))
        pct (min 4 (Math/round (* 4 (/ (.getEnergy wireless-node) (.getMaxEnergy wireless-node)))))
        block-state ^BlockState (.with block-state (get-block-states :connected) true)
        block-state ^BlockState (.with block-state (get-block-states :energy) pct)]
    (.setBlockState world pos block-state 0)))

(deftilerntity tile-node
  :fields {
           :wireless-node nil
           :slots nil
           }
  :post-init (fn [this &args]
               (assoc! this :slots (->LazyOptional (constantly (create-slots this 2)) ) )
               (assoc! this :wireless-node (->LazyOptional (constantly (create-wireless-node this)) ) ))
  :overrides {
              :tick       (fn [this]
                            (let [slots ^ItemStackHandler (.orElse ^LazyOptional (:slots this) nil)
                                  wireless-node (.orElse ^LazyOptional (:wireless-node this) nil)]
                              (when (and slots wireless-node)
                                (update-charge-in (.getStackInSlot slots 0) wireless-node)
                                (update-charge-out (.getStackInSlot slots 1) wireless-node)
                                (rebuild-block-state (.world this) (.getPos this) wireless-node))))
              :createMenu (fn [this, i, ^PlayerInventory playerInventory, ^PlayerEntity playerEntity]
                            (construct container-node i playerInventory playerEntity))
              })

(defn set-placer [tile-entity placer]
  (when (instance? PlayerEntity placer) nil)
  (throw "err"))
