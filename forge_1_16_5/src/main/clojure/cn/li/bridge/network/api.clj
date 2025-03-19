(ns cn.li.bridge.network.api)

(defprotocol IPacketEncoder
  "Packet encoding functionality"
  (encode-message [this message buf] "Encode message to buffer"))

(defprotocol IPacketDecoder
  "Packet decoding functionality"
  (decode-message [this buf] "Decode message from buffer"))

(defprotocol IPacketHandler
  "Packet handling functionality"
  (handle-message [this message ctx] "Handle received message"))

(defprotocol INetworkManager
  "Network management functionality"
  (send-to-server [this message] "Send message to server")
  (send-to-client [this message player] "Send message to client")
  (send-to-all [this message] "Send message to all clients"))

(defn create-resource-location [path]
  nil) ;; Implement in bridge

(defn create-main-channel [bridge]
  nil) ;; Implement in bridge

(defn register-messages [channel]
  nil) ;; Implement in bridge