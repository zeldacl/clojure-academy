(ns cn.academy.block.matrix-network
  (:require [cn.academy.block.matrix-network-cap :as network-cap])
  (:import [net.minecraft.network PacketBuffer]
           [net.minecraftforge.fml.network NetworkEvent$Context NetworkDirection]
           [net.minecraft.util.math BlockPos]))

(defprotocol IMatrixPacket
  (encode [this buf])
  (decode [this buf])
  (handle [this ctx]))

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
  
  (handle [this ctx]
    (let [sender (-> ctx .getSender)
          world (-> sender .getLevel)
          tile (.getTileEntity world (:pos this))]
      (when tile
        (network-cap/handle-sync tile this))
      true)))

(defrecord MatrixNetworkHandler [channel]
  Object
  (registerPackets [_]
    (.registerMessage channel
                     0
                     MatrixSyncPacket
                     #(.encode %1 %2)
                     #(.decode (->MatrixSyncPacket nil nil nil) %)
                     #(.handle %1 %2)
                     (-> (NetworkDirection/PLAY_TO_CLIENT)))))