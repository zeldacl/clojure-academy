(ns forge-impl.network-impl
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraftforge.fml.network NetworkDirection NetworkEvent SimpleChannel]
           [net.minecraft.network PacketBuffer]
           [net.minecraft.entity.player ServerPlayerEntity]))

(defrecord ForgeNetworkHandler [^SimpleChannel channel]
  INetworkHandler
  (send-to-server [_ message]
    (.sendToServer channel message))
  
  (send-to-client [_ message ^ServerPlayerEntity player]
    (.send channel 
          NetworkDirection/PLAY_TO_CLIENT 
          message 
          (.connection player)))
  
  (handle-message [_ message context]
    (.enqueueWork context
                  #((:handler message) message (:player context)))))

(defn create-network-handler [channel]
  (->ForgeNetworkHandler channel))