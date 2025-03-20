(ns cn.academy.block.multiblock.network-sync
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.machine-state :as machine]
            [cn.academy.api.block :as block-api]
            [cn.academy.network :as network]
            [mcmod.protocols :refer :all]
            [clojure.tools.logging :as log])
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

;; Network sync messages for multiblock structures
(defrecord MultiblockUpdateMessage [pos blocks master-pos]
  IPacket
  (encode [this buf]
    (doto buf
      (write-long (pos->long pos))
      (write-long (pos->long master-pos))
      (write-int (count blocks))
      (doseq [block blocks]
        (write-long (pos->long block)))))
      
  (decode [this buf]
    (let [pos (long->pos (read-long buf))
          master (long->pos (read-long buf))
          block-count (read-int buf)
          blocks (vec (repeatedly block-count #(long->pos (read-long buf))))]
      (assoc this 
             :pos pos
             :master-pos master
             :blocks blocks)))
             
  (handle [this ctx]
    (when-let [world (get-world ctx)]
      (when-let [te (get-tile-entity world (:pos this))]
        (when (satisfies? IMultiblock te)
          (doseq [pos (:blocks this)]
            (add-block te pos))
          (set-master te (:master-pos this)))))))

;; Network sync messages for wireless nodes
(defrecord NodeNetworkMessage [pos energy connections]
  IPacket
  (encode [this buf]
    (doto buf
      (write-long (pos->long pos))
      (write-long energy)
      (write-int (count connections))
      (doseq [conn connections]
        (write-long (pos->long conn)))))
        
  (decode [this buf]
    (let [pos (long->pos (read-long buf))
          energy (read-long buf)
          conn-count (read-int buf)
          connections (vec (repeatedly conn-count #(long->pos (read-long buf))))]
      (assoc this
             :pos pos
             :energy energy
             :connections connections)))
             
  (handle [this ctx]
    (when-let [world (get-world ctx)]
      (when-let [te (get-tile-entity world (:pos this))]
        (when (satisfies? IWirelessNode te)
          (receive-energy te (:energy this) false)
          (doseq [conn (:connections this)]
            (connect te conn)))))))

(defn register-network-packets! [network]
  (let [channel (get-channel network)]
    ;; Register multiblock sync packet
    (register-message channel 
                     "multiblock_sync"
                     ->MultiblockUpdateMessage
                     encode
                     decode
                     handle
                     :server->client)
                     
    ;; Register node network packet
    (register-message channel
                     "node_sync" 
                     ->NodeNetworkMessage
                     encode
                     decode
                     handle
                     :server->client)))