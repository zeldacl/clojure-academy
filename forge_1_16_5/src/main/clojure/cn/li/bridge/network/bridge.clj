(ns cn.li.bridge.network.bridge
  (:require [cn.li.bridge.network.api :as network])
  (:import [net.minecraft.network NetworkDirection]
           [net.minecraftforge.fml.network NetworkRegistry NetworkEvent$Context]
           [java.util.function Supplier]))

(defprotocol INetworkBridge
  "Bridge between platform-independent networking and Forge"
  (create-channel [this channel-name]
    "Create a network channel")
  (register-message [this channel message-type encoder decoder handler direction]  
    "Register message type with channel")
  (send-to-server [this channel message]
    "Send message to server")
  (send-to-client [this channel message player]
    "Send message to client")
  (send-to-all [this channel message]
    "Send message to all clients"))

(defrecord ForgeNetworkBridge []
  INetworkBridge
  (create-channel [_ channel-name]
    (NetworkRegistry/newSimpleChannel
     (network/create-resource-location channel-name)))
  
  (register-message [_ channel message-type encoder decoder handler direction]
    (.registerMessage channel
                     message-type
                     (reify java.util.function.Function
                       (apply [_ message]
                         (.encodeMessage encoder message)))
                     (reify java.util.function.Function
                       (apply [_ buf]
                         (.decodeMessage decoder buf)))
                     (reify java.util.function.BiConsumer
                       (accept [_ message ctx]
                         (.handleMessage handler message ctx)))
                     direction))
  
  (send-to-server [_ {:keys [channel]} message]
    (.sendToServer channel message))
  
  (send-to-client [_ {:keys [channel]} message player]
    (.sendTo channel
            message
            (.connection player)
            NetworkDirection/PLAY_TO_CLIENT))
  
  (send-to-all [_ {:keys [channel]} message]
    (let [server (net.minecraft.server.MinecraftServer/getServer)]
      (.send channel
             (.getPacketTarget server)
             message))))

(defn create-network-bridge []
  (->ForgeNetworkBridge))

(defn initialize-networking []
  (let [bridge (create-network-bridge)
        channel (network/create-main-channel bridge)]
    (network/register-messages channel)))