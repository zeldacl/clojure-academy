(ns cn.academy.block.matrix-sync-adapter
  (:require [cn.academy.block.matrix-sync :as sync]
            [cn.academy.block.matrix-position :as position])
  (:import [net.minecraft.network NetworkManager Packet]
           [net.minecraft.network.play.server SPacketParticles]
           [net.minecraftforge.fml.common.network NetworkRegistry FMLNetworkEvent$ServerCustomPacketEvent]
           [net.minecraftforge.fml.common.network.simpleimpl SimpleNetworkWrapper IMessage MessageContext]))

(def NETWORK-INSTANCE (atom nil))

(defprotocol IForgeSyncAdapter
  (send-to-client [this packet player])
  (send-to-server [this packet])
  (handle-packet [this packet world pos])
  (encode-packet [this packet buf])
  (decode-packet [this buf]))

(defrecord MatrixPacket [data]
  IMessage
  (fromBytes [this buf]
    (assoc this :data (decode-packet buf)))
  
  (toBytes [this buf]
    (encode-packet (:data this) buf)))

(defrecord ForgeSyncAdapter [sync-handler pos-adapter channel-name]
  IForgeSyncAdapter
  (send-to-client [_ packet player]
    (let [network (or @NETWORK-INSTANCE
                     (reset! NETWORK-INSTANCE 
                             (NetworkRegistry/INSTANCE createSimpleChannel channel-name)))]
      (.sendTo network 
               (->MatrixPacket packet)
               player)))
  
  (send-to-server [_ packet]
    (let [network (or @NETWORK-INSTANCE
                     (reset! NETWORK-INSTANCE 
                             (NetworkRegistry/INSTANCE createSimpleChannel channel-name)))]
      (.sendToServer network 
                    (->MatrixPacket packet))))
  
  (handle-packet [_ {:keys [type data]} world pos]
    (case type
      :matrix-state 
      (sync/handle-state-update sync-handler data)
      
      :matrix-particles
      (sync/handle-particle-spawn sync-handler 
                                 (update data :pos #(position/from-block-pos pos-adapter %)))))
  
  (encode-packet [_ {:keys [type data]} buf]
    (case type
      :matrix-state
      (doto buf
        (.writeByte 0)
        (.writeNBTTagCompound (encode-state data)))
      
      :matrix-particles
      (let [{:keys [particle-type pos]} data]
        (doto buf
          (.writeByte 1)
          (.writeString (name particle-type))
          (.writeBlockPos (position/to-block-pos pos-adapter pos))))))
  
  (decode-packet [_ buf]
    (case (.readByte buf)
      0 {:type :matrix-state
         :data (decode-state (.readNBTTagCompound buf))}
      1 {:type :matrix-particles
         :data {:particle-type (keyword (.readString buf))
                :pos (position/from-block-pos pos-adapter (.readBlockPos buf))}})))

(defn create-adapter [sync-handler]
  (->ForgeSyncAdapter 
    sync-handler
    (position/create-adapter)
    "academy:matrix"))

(defn register-network-handlers []
  (let [network (NetworkRegistry/INSTANCE createSimpleChannel "academy:matrix")]
    (.registerMessage network 
                     MatrixMessageHandler
                     MatrixPacket
                     0
                     (proxy [java.util.function.Function] []
                       (apply [ctx] 
                         (handle-packet ctx))))))

(defn ^:private handle-packet [^MessageContext ctx]
  (let [packet (.getMessage ctx)
        world (if-let [player (.getServerHandler ctx)]
                (.world player)
                (.world (Minecraft/getMinecraft)))]
    (handle-packet (:data packet) world)))