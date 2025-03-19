(ns cn.li.bridge.matrix.sync
  (:require [cn.li.bridge.matrix.api :as matrix]
            [cn.li.bridge.network.api :as network]
            [clojure.tools.logging :as log])
  (:import [net.minecraft.network NetworkDirection]))

(defrecord MatrixSyncMessage [matrix-id state]
  network/IPacketEncoder
  (encode-message [_ buf]
    (.writeInt buf matrix-id)
    (.writeNbt buf state))
  
  network/IPacketDecoder
  (decode-message [_ buf]
    {:matrix-id (.readInt buf)
     :state (.readNbt buf)}))

(defrecord MatrixSyncHandler [matrix]
  network/INetworkManager
  (send-to-server [_ message]
    (when-let [network-id (matrix/get-network-id matrix)]
      (let [sync-message (->MatrixSyncMessage 
                          network-id 
                          (matrix/sync-with-client matrix))]
        (network/send-packet NetworkDirection/PLAY_TO_SERVER sync-message))))
  
  (send-to-client [_ message player]
    (when-let [network-id (matrix/get-network-id matrix)]
      (let [sync-message (->MatrixSyncMessage 
                          network-id 
                          (matrix/sync-with-client matrix))]
        (network/send-packet NetworkDirection/PLAY_TO_CLIENT sync-message player))))
  
  (send-to-all [_ message]
    (when-let [network-id (matrix/get-network-id matrix)]
      (let [sync-message (->MatrixSyncMessage 
                          network-id 
                          (matrix/sync-with-client matrix))]
        (network/broadcast-packet sync-message))))

  network/IPacketHandler
  (handle-message [_ message ctx]
    (let [{:keys [matrix-id state]} message]
      (when (= matrix-id (matrix/get-network-id matrix))
        (let [old-state (matrix/sync-with-client matrix)]
          (log/debug "Received matrix sync from" 
                    (if (.isClientSide ctx) "client" "server")
                    "- State:" state)
          ; Apply received state
          (matrix/deserialize-from-nbt matrix state)
          ; Notify world of changes if needed
          (when (not= old-state state)
            (matrix/mark-dirty matrix)))))))

(defn create-sync-handler [matrix]
  (->MatrixSyncHandler matrix))

(defn register-sync-handlers [registry]
  (let [message-type (->MatrixSyncMessage 0 nil)]
    (network/register-message registry "matrix_sync" message-type)))