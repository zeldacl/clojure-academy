(ns cn.academy.block.matrix-adapter
  (:require [cn.academy.block.matrix :as matrix])
  (:import [net.minecraft.block Block AbstractBlock$Properties]
           [net.minecraft.block.material Material]
           [net.minecraft.util Direction]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.world World]
           [net.minecraft.entity.player PlayerEntity]))

(defprotocol IForgeMatrixAdapter
  (create-block [this])
  (create-tile-entity [this world pos state])
  (on-block-activated [this world pos state player hand side hit-x hit-y hit-z])
  (on-block-placed [this world pos state placer stack])
  (get-sub-blocks [this]))

(defrecord ForgeMatrixBlock [matrix]
  IForgeMatrixAdapter
  (create-block [_]
    (proxy [Block] [(-> Material/STONE
                        AbstractBlock$Properties/of
                        (.strength 3.0)
                        (.lightLevel (fn [_] 15)))]
      (createTileEntity [world pos state]
        (create-tile-entity world pos state))
      
      (use [world pos state player hand side hit-x hit-y hit-z]
        (on-block-activated world pos state player hand side hit-x hit-y hit-z))
      
      (setPlacedBy [world pos state placer stack]
        (on-block-placed world pos state placer stack))))
  
  (create-tile-entity [_ world pos state]
    (doto (matrix/create-matrix)
      (matrix/set-position! {:x (.getX pos)
                            :y (.getY pos)
                            :z (.getZ pos)})))
  
  (on-block-activated [_ world pos state player hand side hit-x hit-y hit-z]
    (when-not (.isSneaking player)
      (let [center-pos (get-origin world pos)]
        (when center-pos
          (open-gui player world center-pos)
          true))))
  
  (on-block-placed [_ world pos state placer stack]
    (when (instance? PlayerEntity placer)
      (when-let [tile (.getTileEntity world pos)]
        (matrix/set-placer! tile placer))))
  
  (get-sub-blocks [_]
    (map (fn [[x y z]]
           (BlockPos. x y z))
         matrix/matrix-structure)))

(defn create-adapter [matrix]
  (->ForgeMatrixBlock matrix))