(ns cn.academy.energy.capability.wireless-node-capability
  (:require [cn.academy.energy.api.wireless :as wireless])
  (:import [net.minecraftforge.common.capabilities Capability CapabilityManager ICapabilitySerializable]
           [net.minecraft.nbt NBTTagCompound]
           [net.minecraft.util Direction]))

(def ^:private WIRELESS-NODE-CAPABILITY (atom nil))

(defrecord WirelessNodeStorage [energy max-energy range max-connections]
  wireless/IWirelessNode
  (get-range [_] @range)
  
  (get-max-connections [_] @max-connections)
  
  (get-max-energy [_] @max-energy)
  
  (get-energy [_] @energy)
  
  (connect [this other]
    (when (wireless/can-connect? this other)
      (swap! energy #(min (+ % (wireless/get-energy other)) @max-energy))
      true))
  
  (disconnect [_ _]
    (reset! energy 0.0))
  
  (can-connect? [_ other]
    (and (wireless/is-wireless-node? other)
         (< @energy @max-energy)))
  
  ICapabilitySerializable
  (serializeNBT [_]
    (doto (NBTTagCompound.)
      (.setDouble "energy" @energy)
      (.setDouble "maxEnergy" @max-energy)
      (.setDouble "range" @range)
      (.setInteger "maxConnections" @max-connections)))
  
  (deserializeNBT [_ nbt]
    (reset! energy (.getDouble nbt "energy"))
    (reset! max-energy (.getDouble nbt "maxEnergy"))
    (reset! range (.getDouble nbt "range"))
    (reset! max-connections (.getInteger nbt "maxConnections"))))

(defn create-storage [max-energy range max-connections]
  (->WirelessNodeStorage 
    (atom 0.0)
    (atom max-energy)
    (atom range)
    (atom max-connections)))

(defn register! []
  (CapabilityManager/INSTANCE.register
    wireless/IWirelessNode
    (reify Capability$IStorage
      (writeNBT [_ capability instance side]
        (.serializeNBT instance))
      (readNBT [_ capability instance side nbt]
        (.deserializeNBT instance nbt)))
    #(create-storage 100000.0 20.0 4))
  
  (reset! WIRELESS-NODE-CAPABILITY
    (.. (CapabilityManager/INSTANCE)
        (get wireless/IWirelessNode))))

(defn get-capability []
  @WIRELESS-NODE-CAPABILITY)