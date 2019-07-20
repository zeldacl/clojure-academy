(ns cn.li.academy.energy.tileentites.node
  (:import (net.minecraft.entity.player PlayerEntity)))


(defn set-placer [tile-entity placer]
  (when (instance? PlayerEntity placer) nil)
  (throw "err"))
