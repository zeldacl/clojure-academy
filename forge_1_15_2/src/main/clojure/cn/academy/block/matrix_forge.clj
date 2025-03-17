(ns cn.academy.block.matrix-forge
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-sync :as sync])
  (:import [net.minecraft.block.material Material]
           [net.minecraft.tileentity TileEntity]
           [net.minecraft.util Direction Hand]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.nbt CompoundNBT]))

;; 1.15.2 specific protocol for capability handling
(defprotocol IForgeCapabilityMatrix
  (get-capability [this cap side])
  (invalidate-caps [this]))

(defrecord ForgeTileMatrix [matrix sync-handler capabilities]
  IForgeCapabilityMatrix
  (get-capability [_ cap side]
    (when (= cap :wireless-matrix)
      matrix))
  
  (invalidate-caps [_]
    (reset! capabilities {}))

  ;; Standard Forge tile entity methods
  (to-forge-tile [this]
    (let [tile (proxy [TileEntity] []
                 (tick []
                   (sync/update! sync-handler))
                 (write [nbt]
                   (matrix/save-to-nbt matrix nbt))
                 (read [nbt]
                   (matrix/load-from-nbt matrix nbt)))]
      (doto tile
        (.setWorld (:world @(:info matrix)))
        (.setPos (:pos @(:info matrix))))))

  (from-forge-tile [this tile]
    (-> matrix 
        (assoc-in [:info :world] (.getWorld tile))
        (assoc-in [:info :pos] (.getPos tile)))))

(defn create-forge-matrix []
  (let [matrix (matrix/create-matrix)
        sync-handler (sync/->MatrixSyncHandler matrix (atom 0))]
    (->ForgeTileMatrix matrix sync-handler (atom {}))))

;; Network message handlers adjusted for 1.15.2 network system
(defn handle-matrix-sync [tile msg]
  (let [{:keys [plate-count placer-name]} msg
        matrix-forge (from-forge-tile tile)]
    (sync/handle-sync (:sync-handler matrix-forge) plate-count placer-name)))