(ns cn.academy.block.matrix-sync-adapter
  (:require [cn.academy.block.matrix-sync :as sync]
            [cn.academy.block.matrix-position :as position])
  (:import [net.minecraft.network NetworkManager PacketBuffer]
           [net.minecraft.network.play.server SSpawnParticlePacket]
           [net.minecraft.util ResourceLocation]))

(def MATRIX-CHANNEL (ResourceLocation. "academy" "matrix"))

(defprotocol IForgeSyncAdapter
  (send-to-client [this packet player])
  (send-to-server [this packet])
  (handle-packet [this packet world pos])
  (encode-packet [this packet buf])
  (decode-packet [this buf]))

(defrecord ForgeSyncAdapter [sync-handler pos-adapter]
  IForgeSyncAdapter
  (send-to-client [_ packet player]
    (let [network-manager (.getNetworkManager player)]
      (.sendPacket network-manager
                   (SimpleChannel$MessageBuilder.
                     MATRIX-CHANNEL
                     (encode-packet packet (PacketBuffer.))))))
  
  (send-to-server [_ packet]
    (let [client (Minecraft/getInstance)
          network-manager (.getConnection client)]
      (.sendPacket network-manager
                   (SimpleChannel$MessageBuilder.
                     MATRIX-CHANNEL
                     (encode-packet packet (PacketBuffer.))))))
  
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
        (.writeVarInt 0)
        (.writeCompoundTag (encode-state data)))
      
      :matrix-particles
      (let [{:keys [particle-type pos]} data]
        (doto buf
          (.writeVarInt 1)
          (.writeEnumValue particle-type)
          (.writeBlockPos (position/to-block-pos pos-adapter pos))))))
  
  (decode-packet [_ buf]
    (case (.readVarInt buf)
      0 {:type :matrix-state
         :data (decode-state (.readCompoundTag buf))}
      1 {:type :matrix-particles
         :data {:particle-type (.readEnumValue buf)
                :pos (position/from-block-pos pos-adapter (.readBlockPos buf))}})))

(defn create-adapter [sync-handler]
  (->ForgeSyncAdapter sync-handler (position/create-adapter)))