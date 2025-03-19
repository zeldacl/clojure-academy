(ns cn.li.bridge.matrix.network
  (:require [cn.li.bridge.matrix.api :as api]
            [cn.li.bridge.network.api :as network])
  (:import [net.minecraft.network NetworkDirection]
           [net.minecraft.network.play PacketBuffer]
           [net.minecraftforge.fml.network NetworkEvent$Context]))

(defrecord MatrixSyncMessage [matrix-id state]
  network/IPacketEncoder
  (encode-message [_ buf]
    (.writeInt buf matrix-id)
    (.writeNbt buf state))
  
  network/IPacketDecoder
  (decode-message [_ buf]
    {:matrix-id (.readInt buf)
     :state (.readNbt buf)}))

(defrecord MatrixNetworkHandler [matrix]
  network/INetworkManager
  (send-to-server [_ message]
    (when-let [network-id (api/get-network-id matrix)]
      (let [sync-message (->MatrixSyncMessage network-id (api/sync-with-client matrix))]
        (network/send-packet NetworkDirection/PLAY_TO_SERVER sync-message))))
  
  (send-to-client [_ message player]
    (when-let [network-id (api/get-network-id matrix)]
      (let [sync-message (->MatrixSyncMessage network-id (api/sync-with-client matrix))]
        (network/send-packet NetworkDirection/PLAY_TO_CLIENT sync-message player))))
  
  (send-to-all [_ message]
    (when-let [network-id (api/get-network-id matrix)]
      (let [sync-message (->MatrixSyncMessage network-id (api/sync-with-client matrix))]
        (network/broadcast-packet sync-message))))

  network/IPacketHandler
  (handle-message [_ message ctx]
    (let [{:keys [matrix-id state]} message]
      (when (= matrix-id (api/get-network-id matrix))
        (api/deserialize-from-nbt matrix state)))))

(defn create-network-handler [matrix]
  (->MatrixNetworkHandler matrix))

(defn register-network-handlers [registry]
  (let [message-type (->MatrixSyncMessage 0 nil)]
    (network/register-message registry "matrix_sync" message-type)))