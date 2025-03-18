(ns cn.academy.energy.capability.wireless-capability-handler
  (:require [cn.academy.energy.api.wireless :as wireless]
            [cn.academy.energy.api.wireless-registry :as registry])
  (:import [net.minecraftforge.event AttachCapabilitiesEvent]
           [net.minecraftforge.eventbus.api SubscribeEvent]
           [net.minecraft.tileentity TileEntity]
           [net.minecraft.util ResourceLocation]))

(def ^:private WIRELESS_NODE_LOC 
  (ResourceLocation. "cljacademy" "wireless_node"))

(def ^:private WIRELESS_MATRIX_LOC
  (ResourceLocation. "cljacademy" "wireless_matrix"))

(defprotocol ICapabilityHandler
  (handle-attach-capabilities [this event]))

(defrecord WirelessCapabilityHandler []
  ICapabilityHandler
  (handle-attach-capabilities [_ event]
    (when (instance? TileEntity (.getObject event))
      (attach-capabilities! event (.getObject event)))))

(defn attach-capabilities! [event tile]
  (cond 
    (wireless/is-wireless-node? tile)
    (.addCapability event
      WIRELESS_NODE_LOC
      (registry/create-node-provider tile))
    
    (wireless/is-wireless-matrix? tile)
    (.addCapability event
      WIRELESS_MATRIX_LOC
      (registry/create-matrix-provider tile))))

@SubscribeEvent
(defn on-attach-capabilities [^AttachCapabilitiesEvent$TileEntity event]
  (when (instance? TileEntity (.getObject event))
    (attach-capabilities! event (.getObject event))))

(defn create []
  (->WirelessCapabilityHandler))