(ns cn.academy.block.matrix-network
  (:require [cn.academy.block.matrix-network-cap :as network-cap])
  (:import [net.minecraft.network PacketBuffer]
           [net.minecraftforge.fml.network NetworkHooks NetworkRegistry]
           [net.minecraft.util.math BlockPos]))

(defprotocol IMatrixPacket
  (encode [this buf])
  (decode [this buf])
  (handle [this supplier]))

(defrecord MatrixSyncPacket [pos plate-count placer-name]
  IMatrixPacket
  (encode [_ buf]
    (doto buf
      (.writeBlockPos pos)
      (.writeVarInt plate-count)
      (.writeString placer-name)))
  
  (decode [_ buf]
    (->MatrixSyncPacket
      (.readBlockPos buf)
      (.readVarInt buf)
      (.readString buf)))
  
  (handle [this supplier]
    (let [context (.get supplier)
          player (.getSender context)
          world (.getWorld player)
          tile (.getTileEntity world (:pos this))]
      (when tile
        (network-cap/handle-sync tile this)))))

(defrecord MatrixNetworkHandler [channel]
  Object
  (registerPackets [_]
    (NetworkRegistry/registerMessage
      channel
      MatrixSyncPacket
      #(.encode %1 %2)
      #(.decode (->MatrixSyncPacket nil nil nil) %)
      #(.handle %1 %2)
      (.-PLAY_TO_CLIENT NetworkRegistry$TargetPoint))))