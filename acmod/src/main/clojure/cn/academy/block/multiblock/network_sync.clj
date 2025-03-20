(ns cn.academy.block.multiblock.network-sync
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.machine-state :as machine]
            [cn.academy.api.block :as block-api]
            [cn.academy.network :as network])
  (:import [net.minecraft.network PacketBuffer]
           [net.minecraft.util math.BlockPos]))

(defprotocol INetworkSync
  "Protocol for network state synchronization"
  (get-sync-data [this] "Get data to sync")
  (handle-sync-data [this data] "Handle received sync data")
  (should-sync? [this] "Whether this object needs syncing"))

(defn create-sync-packet [pos data]
  (network/->Packet 
    (fn [^PacketBuffer buf]
      (.writeBlockPos buf pos)
      (.writeNbt buf data))
    (fn [^PacketBuffer buf]
      {:pos (.readBlockPos buf)
       :data (.readNbt buf)})))

(defn handle-sync-packet [{:keys [pos data]} world]
  (when-let [te (block-api/get-tile-entity world pos)]
    (when (satisfies? INetworkSync te)
      (handle-sync-data te data))))

(defrecord MultiblockNetworkSync [state-atom last-sync-atom sync-interval]
  INetworkSync
  (get-sync-data [this]
    (when (satisfies? machine/IMachineState this)
      {:energy (machine/get-energy this)
       :active (machine/is-active? this)}))
  
  (handle-sync-data [this data]
    (when (satisfies? machine/IMachineState this)
      (let [{:keys [energy active]} data]
        (reset! state-atom 
                (assoc @state-atom
                       :energy energy
                       :active active)))))
  
  (should-sync? [_]
    (let [current-time (System/currentTimeMillis)
          last-sync @last-sync-atom]
      (when (> (- current-time last-sync) sync-interval)
        (reset! last-sync-atom current-time)
        true)))

  base/IMultiblockMember
  (get-update-packet [this]
    (when (should-sync? this)
      (get-sync-data this)))
  
  (handle-update-packet [this packet]
    (handle-sync-data this packet)))