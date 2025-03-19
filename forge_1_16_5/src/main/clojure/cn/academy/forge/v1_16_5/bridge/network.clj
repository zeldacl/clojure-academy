(ns cn.academy.forge.v1_16_5.bridge.network
  (:require [cn.academy.network.core :as network]
            [cn.academy.forge.v1_16_5.bridge.nbt :as nbt])
  (:import [net.minecraft.network NetworkDirection]
           [net.minecraft.network.play PacketBuffer]
           [net.minecraft.util ResourceLocation]
           [net.minecraftforge.fml.network NetworkRegistry NetworkInstance]
           [net.minecraftforge.fml.network.simple SimpleChannel]
           [io.netty.buffer Unpooled]))

(defprotocol INetworkBridge
  "Bridge between platform-independent networking and Forge"
  (create-channel [this channel-id]
    "Create a new network channel")
  (register-message [this channel msg-id msg-class to-server?]
    "Register a message type with the channel")
  (send-to-server [this channel message]
    "Send a message to the server")
  (send-to-client [this channel message player]
    "Send a message to a specific client")
  (send-to-all [this channel message]
    "Send a message to all clients"))

(deftype ForgeNetworkAdapter [channel message-id->class]
  Object
  (encodeMessage [_ message buf]
    (let [data (network/serialize-message message)
          nbt-data (nbt/create-bridge)
          tag (nbt/write-nbt (nbt/create-nbt) data)]
      (.writeNbt buf tag)))
  
  (decodeMessage [_ buf]
    (let [tag (.readNbt buf)
          data (nbt/read-nbt tag)
          message-class (get @message-id->class (get data :message-id))]
      (when message-class
        (network/deserialize-message message-class data))))
  
  (handleMessage [_ message ctx]
    (.enqueueWork ctx
      (fn []
        (network/handle-message message)))
    (.setPacketHandled ctx true)))

(deftype ForgeNetworkBridge []
  INetworkBridge
  (create-channel [_ channel-id]
    (let [resource-loc (ResourceLocation. "academy" channel-id)
          protocol-version "1.0"
          channel (NetworkRegistry/newSimpleChannel
                   resource-loc
                   #(protocol-version)
                   #(= % protocol-version)
                   #(= % protocol-version))]
      {:channel channel
       :message-id->class (atom {})}))
  
  (register-message [_ {:keys [channel message-id->class]} msg-id msg-class to-server?]
    (let [direction (if to-server?
                      NetworkDirection/PLAY_TO_SERVER
                      NetworkDirection/PLAY_TO_CLIENT)
          adapter (ForgeNetworkAdapter. channel message-id->class)]
      (swap! message-id->class assoc msg-id msg-class)
      (.registerMessage channel
                       msg-id
                       (class msg-class)
                       (reify java.util.function.BiConsumer
                         (accept [_ message buf]
                           (.encodeMessage adapter message buf)))
                       (reify java.util.function.Function
                         (apply [_ buf]
                           (.decodeMessage adapter buf)))
                       (reify java.util.function.BiConsumer
                         (accept [_ message ctx]
                           (.handleMessage adapter message ctx)))
                       direction)))
  
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

(defn create-bridge []
  (->ForgeNetworkBridge))

(defonce main-channel (atom nil))

(defn initialize-networking []
  (let [bridge (create-bridge)
        channel (.create-channel bridge "main")]
    (reset! main-channel channel)
    (network/register-network-impl 
     {:send-to-server #(.send-to-server bridge @main-channel %)
      :send-to-client #(.send-to-client bridge @main-channel %1 %2)
      :send-to-all #(.send-to-all bridge @main-channel %)})))

(defn register-message [msg-id msg-class to-server?]
  (let [bridge (create-bridge)]
    (.register-message bridge @main-channel msg-id msg-class to-server?)))