(ns cn.li.bridge.matrix.block-component
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-state :as state])
  (:import [net.minecraft.block Block BlockState]
           [net.minecraft.world World]
           [net.minecraft.util.math BlockPos]
           [net.minecraftforge.common MinecraftForge]))

(defprotocol IMatrixBlockComponent
  "Protocol for Matrix block behaviors in Forge"
  (on-placed [this world pos player] "Handle block placement")
  (on-broken [this world pos state] "Handle block being broken")
  (on-neighbor-changed [this world pos state neighbor] "Handle neighbor changes")
  (get-matrix [this] "Get associated Matrix instance"))

(defrecord MatrixBlockComponent [matrix]
  IMatrixBlockComponent
  (on-placed [_ world pos player]
    (when-not (.isClientSide world)
      (matrix/on-placed matrix pos {:player player})))
  
  (on-broken [_ world pos state]
    (when-not (.isClientSide world)
      (matrix/on-removed matrix pos)))
  
  (on-neighbor-changed [_ world pos state neighbor]
    (when-not (.isClientSide world)
      (state/validate-structure! matrix)))
  
  (get-matrix [_] matrix))

(defn create-block-component [matrix]
  (->MatrixBlockComponent matrix))