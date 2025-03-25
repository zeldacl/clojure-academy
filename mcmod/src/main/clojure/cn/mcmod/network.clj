(ns mcmod.network
  (:require [mcmod.protocols :refer :all]))

(defprotocol IPacket
  "Protocol for network packets"
  (encode [this buf] "Encode packet data to buffer")
  (decode [this buf] "Decode packet data from buffer")
  (handle [this ctx] "Handle packet on receiving side"))

(defn register-packet [registry packet-id packet-type handler]
  (swap! (:packets registry) assoc packet-id
         {:type packet-type
          :handler handler}))

(defn get-packet-handler [registry packet-id]
  (get-in @(:packets registry) [packet-id :handler]))

(defn create-registry []
  {:packets (atom {})})