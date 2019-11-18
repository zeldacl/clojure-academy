(ns cn.li.academy.energy.tileentites.node
  (:require [cn.li.mcmod.tileentity :refer [deftilerntity]]
            [cn.li.mcmod.blocks :refer [get-block-states]]
            [cn.li.academy.energy.utils :refer [imag-energy-item?]])
  (:import (net.minecraft.entity.player PlayerEntity)
           (net.minecraftforge.items ItemStackHandler)
           (net.minecraft.tileentity TileEntity)
           (cn.li.academy.api.energy.capability WirelessNode)
           (net.minecraft.block BlockState)))


(defn create-slots [tile size]
  (proxy [ItemStackHandler] [size]
    (onContentsChanged [slot]
      (.markDirty ^TileEntity tile))
    (isItemValid [slot, stack]
      (imag-energy-item? stack))))

(defn create-wireless-node [tile]
  (proxy [WirelessNode] []
    (getMaxEnergy []
      (let [block-state (.getBlockState tile)
            node-type (.get ^BlockState block-state (get-block-states :node-type))]))))


(deftilerntity tile-node
  :fields {
           :energy 0
           }
  :post-init (fn [this &args]
               )
  :overrides {
              :tick (fn [this]
                      (let [world (.-world this)])
                      )
              })

(defn set-placer [tile-entity placer]
  (when (instance? PlayerEntity placer) nil)
  (throw "err"))
