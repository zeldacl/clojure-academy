(ns cn.mcmod.forge-modern.network
  (:require [cn.mcmod.core.network.messages :as messages])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraftforge.fml.network NetworkRegistry NetworkDirection NetworkEvent$Context]
           [net.minecraftforge.fml.network.simple SimpleChannel]))

(defn- get-protocol-version [mod-version]
  (str mod-version))

(defn create-network-channel [mod-id mod-version]
  (let [channel-name (ResourceLocation. mod-id "main")
        protocol-version (get-protocol-version mod-version)]
    (-> (NetworkRegistry/newSimpleChannel
          channel-name
          #(protocol-version)
          #(= % protocol-version)
          #(= % protocol-version))
        (.messageBuilder messages/CatEngineUpdateMessage 0)
        (.encoder #(messages/encode %1 %2))
        (.decoder #(messages/decode (messages/->CatEngineUpdateMessage nil 0.0 false) %))
        (.consumer (reify java.util.function.BiConsumer
                    (accept [_ msg ctx]
                      (let [sender (.getSender ^NetworkEvent$Context ctx)]
                        (messages/handle msg {:player sender 
                                           :world (.getLevel sender)})))))
        (.add))))

(defn send-to-all! [^SimpleChannel channel message]
  (.send channel 
         NetworkDirection/PLAY_TO_CLIENT 
         message))

(defn send-to-player! [^SimpleChannel channel message player]
  (.sendTo channel 
           message 
           (.connection player)
           NetworkDirection/PLAY_TO_CLIENT))