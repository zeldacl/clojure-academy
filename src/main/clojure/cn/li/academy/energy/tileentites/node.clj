(ns cn.li.academy.energy.tileentites.node
  (:require [cn.li.mcmod.tileentity :refer [deftilerntity]])
  (:import (net.minecraft.entity.player PlayerEntity)
           (net.minecraftforge.items ItemStackHandler)))


(defn create-ss [size]
  (proxy [ItemStackHandler] [size]
    ()))


(deftilerntity tile-node
  :fields {
           :energy 0
           }
  :overrides {
              :tick (fn [this]
                      (let [world (.-world this)])
                      )
              })

(defn set-placer [tile-entity placer]
  (when (instance? PlayerEntity placer) nil)
  (throw "err"))
