(ns cn.mcmod.network
  (:require [cn.mcmod.logging :as log]
            [cn.mcmod.protocols :refer :all])
  (:import [java.util.function BiConsumer Function]
           [java.nio ByteBuffer]))

;; Dynamic binding to hold current forge version network implementation
(def ^:dynamic *network-impl* nil)

;; Set the appropriate network implementation
(defn set-network-impl! [impl]
  (alter-var-root #'*network-impl* (constantly impl)))

(defprotocol INetworkChannel
  (register-message [this message-type id encoder decoder handler] "Register a message type with the channel")
  (send-to-server [this message] "Send a message to the server")
  (send-to-client [this message player] "Send a message to a specific client")
  (send-to-all [this message] "Send a message to all clients"))

(defprotocol INetworkMessage
  (encode [this buffer] "Encode message to buffer")
  (decode [this buffer] "Decode message from buffer")
  (handle [this ctx] "Handle message on the receiving side"))

;; Default implementations for common message types
(defrecord SimpleNetworkMessage [message-type data]
  INetworkMessage
  (encode [this buffer]
    (case message-type
      :string (.putString buffer data)
      :int (.putInt buffer data)
      :long (.putLong buffer data)
      :boolean (.putBoolean buffer data)
      :double (.putDouble buffer data)
      (log/error "Unknown message type: %s" message-type)))
  
  (decode [this buffer]
    (assoc this :data 
      (case message-type
        :string (.getString buffer)
        :int (.getInt buffer)
        :long (.getLong buffer)
        :boolean (.getBoolean buffer)
        :double (.getDouble buffer)
        (log/error "Unknown message type: %s" message-type))))
  
  (handle [this ctx]
    (log/debug "Handled message type %s with data: %s" 
               message-type data)))

;; Network channel implementation - version agnostic abstraction
(defrecord NetworkChannelImpl [channel-id handler-registry]
  INetworkChannel
  (register-message [this message-type id encoder decoder handler]
    (swap! handler-registry assoc id 
           {:message-type message-type
            :encoder encoder
            :decoder decoder
            :handler handler})
    this)
  
  (send-to-server [this message]
    (let [type-id (get-in @handler-registry [(.getClass message) :id])]
      (when (and type-id *network-impl*)
        ((:send-to-server *network-impl*) channel-id message))))
  
  (send-to-client [this message player]
    (let [type-id (get-in @handler-registry [(.getClass message) :id])]
      (when (and type-id *network-impl*)
        ((:send-to-client *network-impl*) channel-id message player))))
  
  (send-to-all [this message]
    (let [type-id (get-in @handler-registry [(.getClass message) :id])]
      (when (and type-id *network-impl*)
        ((:send-to-all *network-impl*) channel-id message)))))

;; Forge version-specific adapters would go here
;; These are implemented in forge_modern.network and forge_legacy.network

;; Helper for creating simple string messages
(defn create-string-message [data]
  (->SimpleNetworkMessage :string data))

;; Helper for creating simple numeric messages  
(defn create-numeric-message [type data]
  (->SimpleNetworkMessage type data))

;; Create a network channel with the specified ID
(defn create-network-channel [channel-id]
  (->NetworkChannelImpl channel-id (atom {})))