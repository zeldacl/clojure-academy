(ns cn.academy.forge-1-12.network
  (:require [cn.academy.core.network.messages :as messages])
  (:import [net.minecraftforge.fml.common.network NetworkRegistry]
           [net.minecraftforge.fml.common.network.simpleimpl SimpleNetworkWrapper IMessage IMessageHandler]
           [net.minecraftforge.fml.relauncher Side]
           [io.netty.buffer ByteBuf]))

(defn create-message-handler [message-type]
  (reify IMessageHandler
    (onMessage [_ message context]
      (let [player (.getServerHandler (.getThreadListener context))
            world (.world player)]
        (messages/handle message {:player player :world world}))
      nil)))

(defn create-network-wrapper [mod-id]
  (let [channel (NetworkRegistry/INSTANCE)
        wrapper (SimpleNetworkWrapper. mod-id)]
    (.registerMessage wrapper 
                     (create-message-handler messages/CatEngineUpdateMessage)
                     messages/CatEngineUpdateMessage
                     0
                     Side/CLIENT)
    wrapper))

(defn send-to-all! [^SimpleNetworkWrapper wrapper message]
  (.sendToAll wrapper message))

(defn send-to-player! [^SimpleNetworkWrapper wrapper message player]
  (.sendTo wrapper message player))