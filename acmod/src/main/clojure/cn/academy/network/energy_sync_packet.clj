(ns cn.academy.network.energy-sync-packet
  (:require [mcmod.network :refer [IPacket]])
  (:import [net.minecraft.network PacketBuffer]
           [net.minecraft.util.math BlockPos]))

(defrecord EnergySyncPacket [pos energy-stored]
  IPacket
  (encode [this buf]
    (doto buf
      (.writeInt (:x pos))
      (.writeInt (:y pos))
      (.writeInt (:z pos))
      (.writeInt energy-stored)))
  
  (decode [this buf]
    (->EnergySyncPacket
     {:x (.readInt buf)
      :y (.readInt buf)
      :z (.readInt buf)}
     (.readInt buf)))
  
  (handle [this ctx]
    (let [world (.. ctx getPlayer world)
          block-pos (BlockPos. (:x pos) (:y pos) (:z pos))
          tile-entity (.getTileEntity world block-pos)]
      (when tile-entity
        ;; Update client-side energy storage
        (when-let [energy-storage (.getCapability tile-entity net.minecraftforge.energy.CapabilityEnergy/ENERGY nil)]
          (.deserializeNBT energy-storage {"energy" energy-stored}))))))