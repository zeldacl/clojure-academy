(ns cn.li.bridge.matrix.block-component
  (:require [cn.li.bridge.matrix.api :as matrix]
            [cn.li.bridge.matrix.structure :as structure]
            [cn.li.bridge.matrix.event-coordinator :as coordinator]
            [clojure.tools.logging :as log])
  (:import [net.minecraft.block Block BlockState]
           [net.minecraft.tileentity TileEntity]
           [net.minecraft.world World]
           [net.minecraft.util.math BlockPos]
           [net.minecraftforge.common MinecraftForge]))

(defprotocol IMatrixBlock
  "Protocol for Matrix block behaviors"
  (on-placed [this world pos player] "Handle block placement")
  (on-broken [this world pos state] "Handle block being broken")
  (on-neighbor-changed [this world pos state neighbor] "Handle neighbor changes")
  (get-matrix [this] "Get associated Matrix instance"))

(defrecord MatrixCoreBlock [matrix coordinator]
  IMatrixBlock
  (on-placed [_ world pos player]
    (when-not (.isClientSide world)
      (let [structure-valid? (structure/validate-matrix-structure world pos)]
        (when structure-valid?
          (coordinator/coordinate-structure-update coordinator matrix pos)))))
  
  (on-broken [_ world pos state]
    (when-not (.isClientSide world)
      (matrix/unregister-matrix! matrix)
      (structure/break-structure matrix world pos)))
  
  (on-neighbor-changed [_ world pos state neighbor]
    (when-not (.isClientSide world)
      (let [structure-valid? (structure/validate-matrix-structure world pos)]
        (coordinator/coordinate-structure-update coordinator matrix pos))))
  
  (get-matrix [_] matrix))

(defrecord MatrixPlateBlock [matrix coordinator]
  IMatrixBlock
  (on-placed [_ world pos player]
    (when-not (.isClientSide world)
      (when-let [core-pos (structure/find-core-position world pos)]
        (coordinator/coordinate-structure-update coordinator matrix core-pos))))
  
  (on-broken [_ world pos state]
    (when-not (.isClientSide world)
      (when-let [core-pos (structure/find-core-position world pos)]
        (coordinator/coordinate-structure-update coordinator matrix core-pos))))
  
  (on-neighbor-changed [_ world pos state neighbor]
    (when-not (.isClientSide world)
      (when-let [core-pos (structure/find-core-position world pos)]
        (coordinator/coordinate-structure-update coordinator matrix core-pos))))
  
  (get-matrix [_] matrix))

(defn create-core-block [matrix coordinator]
  (->MatrixCoreBlock matrix coordinator))

(defn create-plate-block [matrix coordinator]
  (->MatrixPlateBlock matrix coordinator))

(defn register-block-events! [block]
  (.register MinecraftForge/EVENT_BUS block))