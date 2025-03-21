(ns cn.academy.network.energy-sync-packet
  (:require [mcmod.network :refer [IPacket]]
            [mcmod.capabilities :as cap]
            [mcmod.world :as world]))

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
    (let [world (world/get-world ctx)
          block-pos {:x (:x pos), :y (:y pos), :z (:z pos)}
          tile-entity (world/get-tile-entity world block-pos)]
      (when tile-entity
        ;; Update client-side energy storage
        (when-let [energy-storage (cap/get-energy-capability tile-entity nil)]
          (cap/deserialize-nbt energy-storage {"energy" energy-stored}))))))