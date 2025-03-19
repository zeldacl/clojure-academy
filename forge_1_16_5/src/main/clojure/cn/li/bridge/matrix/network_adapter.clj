(ns cn.li.bridge.matrix.network-adapter
  (:require [cn.li.bridge.matrix.api :as matrix]
            [cn.li.bridge.network.api :as network]
            [cn.li.bridge.matrix.network-events :refer [->MatrixNetworkSyncEvent]])
  (:import [net.minecraft.network NetworkDirection]
           [net.minecraft.network.play PacketBuffer]))

(defrecord MatrixSyncPacket [matrix-id state]
  network/IPacketEncoder
  (encode [_ buf]
    (.writeVarInt buf matrix-id)
    (.writeNbt buf state))
  
  network/IPacketDecoder
  (decode [_ buf]
    (->MatrixSyncPacket
      (.readVarInt buf)
      (.readNbt buf))))

(defprotocol INetworkAdapter
  "Protocol for Matrix network communication"
  (sync-to-client [this player] "Sync matrix state to client")
  (sync-to-server [this] "Sync matrix state to server") 
  (handle-sync [this packet] "Handle incoming sync packet"))

(defrecord MatrixNetworkAdapter [matrix network-handler]
  INetworkAdapter
  (sync-to-client [_ player]
    (let [state (matrix/get-sync-data matrix)
          packet (->MatrixSyncPacket 
                  (matrix/get-id matrix)
                  state)]
      (network/send-to-player network-handler player packet)))
  
  (sync-to-server [_]
    (let [state (matrix/get-sync-data matrix)
          packet (->MatrixSyncPacket
                  (matrix/get-id matrix) 
                  state)]
      (network/send-to-server network-handler packet)))
  
  (handle-sync [_ packet]
    (let [{:keys [matrix-id state]} packet]
      (when (= matrix-id (matrix/get-id matrix))
        (matrix/handle-sync matrix state)))))

(defn create-network-adapter [matrix network-handler]
  (->MatrixNetworkAdapter matrix network-handler))