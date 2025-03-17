(ns cn.academy.block.matrix-block
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.multi-block :as multi])
  (:import [net.minecraft.block.material Material]
           [net.minecraft.util EnumFacing EnumHand]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.world World]
           [net.minecraft.entity.player EntityPlayer]))

(defprotocol IForgeMatrix
  (create-tile-entity [this world meta])
  (get-container [this player world x y z]))

(defrecord ForgeMatrixBlock [core gui-handler]
  IForgeMatrix
  (create-tile-entity [_ world meta]
    (let [tile (matrix/create-matrix)]
      (doto tile
        (-> :info deref (assoc :world world)))))
  
  (get-container [_ player world x y z]
    (when-let [tile (.getTileEntity world (BlockPos. x y z))]
      (when (instance? cn.academy.block.tileentity.TileMatrix tile)
        (create-container tile player)))))

(defn create-matrix-block [gui-handler]
  (->ForgeMatrixBlock (matrix/create-matrix) gui-handler))

;; Container implementation
(defrecord MatrixContainer [tile player]
  IContainer
  (init-inventory [this]
    ;; Add matrix slots
    (doto this
      (add-slot "PLATE" tile 0 78 11)
      (add-slot "PLATE" tile 1 53 60)
      (add-slot "PLATE" tile 2 104 60)
      (add-slot "CORE" tile 3 78 36)
      (map-player-inventory))))