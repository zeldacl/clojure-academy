(ns cljacademy.blocks.matrix
  (:require [cljacademy.blocks.multiblock :as multiblock]
            [cljacademy.api.block :as block]
            [cljacademy.api.tileentity :as te]))

(defn create-matrix-block []
  (let [block-props {:material :rock
                     :hardness 3.0
                     :light-level 1.0}
        sub-blocks [[0 0 1]
                   [1 0 1]
                   [1 0 0]
                   [0 1 0]
                   [0 1 1]
                   [1 1 1]
                   [1 1 0]]]
    (multiblock/create-multiblock block-props sub-blocks)))

(defn handle-block-activated [world pos state player]
  (when-not (.isSneaking player)
    (when-let [origin (multiblock/get-origin world pos)]
      (block/open-gui player world origin)
      true)))

(defn on-block-placed [world pos state placer stack]
  (when (instance? net.minecraft.entity.player.EntityPlayer placer)
    (when-let [tile (te/get-tile-entity world pos)]
      (.setPlacer tile placer))))