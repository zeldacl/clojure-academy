(ns cn.academy.block.multiblock.network-sync
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.protocols.network :as net]
            [cn.academy.protocols.block :as block-api]
            [clojure.tools.logging :as log]))

(defprotocol INetworkSync
  "Protocol for network state synchronization"
  (get-sync-data [this] "Get data to sync")
  (handle-sync-data [this data] "Handle received sync data")
  (should-sync? [this] "Whether this object needs syncing"))

(defn create-sync-packet [pos data]
  (network/->Packet 
    (fn [buf]
      (net/write-block-pos buf pos)
      (net/write-nbt buf data))
    (fn [buf]
      {:pos (net/read-block-pos buf)
       :data (net/read-nbt buf)})))

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
  net/IPacket
  (encode [this buf]
    (doto buf
      (net/write-long (position/to-long pos))
      (net/write-long (position/to-long master-pos))
      (net/write-int (count blocks))
      (doseq [block blocks]
        (net/write-long (position/to-long block)))))
      
  (decode [this buf]
    (let [pos (position/from-long (net/read-long buf))
          master (position/from-long (net/read-long buf))
          block-count (net/read-int buf)
          blocks (vec (repeatedly block-count #(position/from-long (net/read-long buf))))]
      (assoc this 
             :pos pos
             :master-pos master
             :blocks blocks)))
             
  (handle [this ctx]
    (when-let [world (net/get-world ctx)]
      (when-let [te (block-api/get-tile-entity world (:pos this))]
        (when (satisfies? base/IMultiblock te)
          (doseq [pos (:blocks this)]
            (base/add-block te pos))
          (base/set-master te (:master-pos this)))))))

;; Network sync messages for wireless nodes
(defrecord NodeNetworkMessage [pos energy connections]
  net/IPacket
  (encode [this buf]
    (doto buf
      (net/write-long (position/to-long pos))
      (net/write-long energy)
      (net/write-int (count connections))
      (doseq [conn connections]
        (net/write-long (position/to-long conn)))))
        
  (decode [this buf]
    (let [pos (position/from-long (net/read-long buf))
          energy (net/read-long buf)
          conn-count (net/read-int buf)
          connections (vec (repeatedly conn-count #(position/from-long (net/read-long buf))))]
      (assoc this
             :pos pos
             :energy energy
             :connections connections)))
             
  (handle [this ctx]
    (when-let [world (net/get-world ctx)]
      (when-let [te (block-api/get-tile-entity world (:pos this))]
        (when (satisfies? base/IWirelessNode te)
          (base/receive-energy te (:energy this) false)
          (doseq [conn (:connections this)]
            (base/connect te conn)))))))

(defn register-network-packets! [network]
  (let [channel (net/get-channel network)]
    ;; Register multiblock sync packet
    (net/register-message channel 
                     "multiblock_sync"
                     ->MultiblockUpdateMessage
                     :server->client)
                     
    ;; Register node network packet
    (net/register-message channel
                     "node_sync" 
                     ->NodeNetworkMessage
                     :server->client)))