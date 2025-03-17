(ns cn.academy.block.matrix-adapter
  (:require [cn.academy.block.matrix :as matrix])
  (:import [net.minecraft.block Block]
           [net.minecraft.block.material Material]
           [net.minecraft.util EnumFacing EnumHand]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.world World]
           [net.minecraft.entity.player EntityPlayer]))

(defprotocol IForgeMatrixAdapter
  (create-block [this])
  (create-tile-entity [this world meta])
  (on-block-activated [this world pos state player hand side hit-x hit-y hit-z])
  (on-block-placed [this world pos state placer stack])
  (get-sub-blocks [this]))

(defrecord ForgeMatrixBlock [matrix]
  IForgeMatrixAdapter
  (create-block [_]
    (proxy [Block] [Material/ROCK]
      (createNewTileEntity [world meta]
        (create-tile-entity world meta))
      
      (onBlockActivated [world pos state player hand side hit-x hit-y hit-z]
        (on-block-activated world pos state player hand side hit-x hit-y hit-z))
      
      (onBlockPlacedBy [world pos state placer stack]
        (on-block-placed world pos state placer stack))
      
      (init []  ; Initialize block properties in 1.12.2 style
        (doto this
          (.setHardness 3.0)
          (.setLightLevel 1.0)))))
  
  (create-tile-entity [_ world meta]
    (matrix/create-matrix))
  
  (on-block-activated [_ world pos state player hand side hit-x hit-y hit-z]
    (when-not (.isSneaking player)
      (let [center-pos (get-origin world pos)]
        (when center-pos
          (open-gui player world center-pos)
          true))))
  
  (on-block-placed [_ world pos state placer stack]
    (when (instance? EntityPlayer placer)
      (when-let [tile (.getTileEntity world pos)]
        (matrix/set-placer! tile placer))))
  
  (get-sub-blocks [_]
    (map (fn [[x y z]]
           (BlockPos. x y z))
         matrix/matrix-structure)))

(defn create-adapter [matrix]
  (->ForgeMatrixBlock matrix))