(ns cn.academy.block.matrix-forge
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-sync :as sync])
  (:import [net.minecraft.block.material Material]
           [net.minecraft.tileentity TileEntity]
           [net.minecraft.util EnumFacing EnumHand]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.entity.player EntityPlayer]
           [net.minecraft.nbt NBTTagCompound]))

(defprotocol IForgeTileMatrix
  (to-forge-tile [this])
  (from-forge-tile [this tile]))

(defrecord ForgeTileMatrix [matrix sync-handler]
  IForgeTileMatrix
  (to-forge-tile [this]
    (let [tile (proxy [TileEntity] []
                 (update []
                   (sync/update! sync-handler))
                 (writeToNBT [nbt]
                   (matrix/save-to-nbt matrix nbt))
                 (readFromNBT [nbt]
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
    (->ForgeTileMatrix matrix sync-handler)))

;; Network message handlers
(defn handle-matrix-sync [tile msg]
  (let [{:keys [plate-count placer-name]} msg
        matrix-forge (from-forge-tile tile)]
    (sync/handle-sync (:sync-handler matrix-forge) plate-count placer-name)))