(ns cn.academy.block.forge-matrix
  (:require [cn.academy.block.matrix-core :as core])
  (:import [net.minecraft.block.material Material]
           [net.minecraft.util EnumFacing EnumHand]
           [net.minecraft.util.math BlockPos]))

(defn create-block-state [core-block block-state]
  (let [matrix-core (core/create-matrix-block)]
    (doto block-state
      (.setHardness (core/get-hardness matrix-core))
      (.setLightLevel (core/get-light-level matrix-core))))

(defn create-tile-entity [world meta]
  (core/create-matrix-tile))

(defn handle-block-activated [world pos state player hand facing hit-x hit-y hit-z]
  (when-not (.isSneaking player)
    (let [center (get-origin world pos)]
      (when center
        (open-gui-container player world 
                           (.getX center) 
                           (.getY center) 
                           (.getZ center))
        true))))

(defn handle-block-placed [world pos state placer stack]
  (when (instance? net.minecraft.entity.player.EntityPlayer placer)
    (when-let [tile (.getTileEntity world pos)]
      (when (instance? cn.academy.block.tileentity.TileMatrix tile)
        (core/set-placer! tile placer)))))