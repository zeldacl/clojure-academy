(ns cn.academy.forge.network
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core])
  (:import [net.minecraft.network NetworkManager NetworkDirection PacketBuffer]
           [net.minecraftforge.fml.network NetworkRegistry NetworkEvent$Context SimpleChannel]))

(defrecord ForgeNetworkBridge [mod-id]
  INetworkBridge
  (create-channel [this channel-name]
    (let [version "1.0"
          channel (NetworkRegistry/newSimpleChannel
                   (ResourceLocation. mod-id channel-name)
                   #(version)
                   #(= % version)
                   #(= % version))]
      {:channel channel
       :message-id (atom 0)}))
       
  (register-message [this {:keys [channel message-id]} message-type encoder decoder handler direction]
    (let [network-direction (case direction
                            :client->server NetworkDirection/PLAY_TO_SERVER
                            :server->client NetworkDirection/PLAY_TO_CLIENT)]
      (.registerMessage channel 
                       (swap! message-id inc)
                       (type message-type)
                       (reify BiConsumer
                         (accept [this msg buf]
                           (encoder msg buf)))
                       (reify Function  
                         (apply [this buf]
                           (decoder buf)))
                       (reify BiConsumer
                         (accept [this msg ctx]
                           (handler msg ctx)))
                       network-direction)))
                       
  (send-to-server [this channel message]
    (let [^SimpleChannel network-channel (:channel channel)]
      (.sendToServer network-channel message)))
      
  (send-to-client [this channel message player]
    (let [^SimpleChannel network-channel (:channel channel)]
      (.sendTo network-channel message 
              (.connection player) 
              NetworkDirection/PLAY_TO_CLIENT)))
              
  (send-to-all [this channel message]
    (let [^SimpleChannel network-channel (:channel channel)]
      (.send network-channel 
            PacketTarget/ALL
            message))))

(defn create-network-bridge [mod-id]
  (->ForgeNetworkBridge mod-id))

;; Utility functions for packet buffer handling
(extend-protocol IBuffer
  PacketBuffer
  (write-long [this value]
    (.writeLong this value))
    
  (read-long [this]
    (.readLong this))
    
  (write-double [this value]
    (.writeDouble this value))
    
  (read-double [this]
    (.readDouble this))
    
  (write-boolean [this value]
    (.writeBoolean this value))
    
  (read-boolean [this]
    (.readBoolean this))
    
  (write-string [this value]
    (.writeString this value))
    
  (read-string [this]
    (.readString this)))