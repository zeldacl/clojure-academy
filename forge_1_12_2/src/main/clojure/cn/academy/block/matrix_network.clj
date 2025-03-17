(ns cn.academy.block.matrix-network
  (:require [cn.academy.block.matrix-network-cap :as network-cap])
  (:import [net.minecraft.network PacketBuffer]
           [net.minecraftforge.fml.common.network NetworkRegistry SimpleNetworkWrapper]
           [net.minecraftforge.fml.common.network.simpleimpl IMessage IMessageHandler]
           [net.minecraft.util.math BlockPos]))

(defrecord MatrixSyncMessage [pos plate-count placer-name]
  IMessage
  (fromBytes [this buf]
    (->MatrixSyncMessage
      (.readBlockPos buf)
      (.readVarIntFromBuffer buf)
      (.readStringFromBuffer buf 32767)))
  
  (toBytes [this buf]
    (doto buf
      (.writeBlockPos pos)
      (.writeVarIntToBuffer plate-count)
      (.writeStringToBuffer placer-name))))

(defrecord MatrixSyncHandler []
  IMessageHandler
  (onMessage [_ message context]
    (let [player (.getServerHandler context)
          world (.world player)
          tile (.getTileEntity world (.pos message))]
      (when tile
        (network-cap/handle-sync tile message))
      nil)))

(defrecord MatrixNetworkHandler [channel]
  Object
  (registerPackets [_]
    (let [network (NetworkRegistry/INSTANCE)
          channel (SimpleNetworkWrapper. "academy_matrix")]
      (.registerMessage channel
                       MatrixSyncHandler
                       MatrixSyncMessage
                       0
                       (.-CLIENT Side)))))