(ns cn.academy.block.matrix-forge
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-sync :as sync])
  (:import [net.minecraft.block.material Material]
           [net.minecraft.tileentity TileEntity]
           [net.minecraft.util Direction Hand]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.nbt CompoundNBT]
           [net.minecraftforge.common.capabilities Capability]))

(defprotocol IForgeCapabilityMatrix
  (get-capability [this cap side])
  (invalidate-caps [this]))

(defrecord ForgeTileMatrix [matrix sync-handler capabilities]
  IForgeCapabilityMatrix
  (get-capability [_ cap side]
    (when (instance? Capability cap)
      (when (= (.getName cap) "academy:wireless_matrix")
        matrix)))
  
  (invalidate-caps [_]
    (reset! capabilities {}))

  ;; Standard Forge tile entity methods for 1.16.5
  (to-forge-tile [this]
    (let [tile (proxy [TileEntity] []
                 (tick []
                   (sync/update! sync-handler))
                 (save [nbt]
                   (matrix/save-to-nbt matrix nbt))
                 (load [block-state nbt]
                   (matrix/load-from-nbt matrix nbt)))]
      (doto tile
        (.setLevel (:world @(:info matrix)))
        (.setBlockPos (:pos @(:info matrix))))))

  (from-forge-tile [this tile]
    (-> matrix 
        (assoc-in [:info :world] (.getLevel tile))
        (assoc-in [:info :pos] (.getBlockPos tile)))))

(defn create-forge-matrix []
  (let [matrix (matrix/create-matrix)
        sync-handler (sync/->MatrixSyncHandler matrix (atom 0))]
    (->ForgeTileMatrix matrix sync-handler (atom {}))))

;; Network message handlers adapted for 1.16.5 network system
(defn handle-matrix-sync [tile msg]
  (let [{:keys [plate-count placer-name]} msg
        matrix-forge (from-forge-tile tile)]
    (sync/handle-sync (:sync-handler matrix-forge) plate-count placer-name)))