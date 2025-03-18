(ns cn.academy.energy.api.wireless-helper
  (:require [cn.academy.api.energy :as energy]
            [cn.academy.energy.impl.wireless-world-data :as world-data]
            [cn.academy.energy.impl.node-connection :as node-conn]
            [cn.academy.energy.api.wireless-events :as events])
  (:import [net.minecraft.tileentity TileEntity]
           [net.minecraft.util.math BlockPos]))

(defprotocol IWirelessNode
  (get-node-name [this])
  (get-max-energy [this])
  (get-current-energy [this])
  (receive-energy [this amount])
  (get-range [this]))

(defprotocol IWirelessGenerator
  (has-node? [this])
  (get-linked-node [this])
  (link-to-node! [this node])
  (unlink! [this]))

(defn get-wireless-net
  "Get the wireless network associated with a matrix or node"
  ([matrix]
   (when-let [tile ^TileEntity matrix]
     (world-data/get-network-by-matrix 
       (world-data/get (.getWorld tile))
       matrix)))
  ([node]
   (when-let [tile ^TileEntity node]
     (world-data/get-network-by-node
       (world-data/get (.getWorld tile))
       node))))

(defn is-node-linked? [node]
  (boolean (get-wireless-net node)))

(defn is-matrix-active? [matrix]
  (boolean (get-wireless-net matrix)))

(defn get-node-conn
  "Get the node connection for a node or user"
  ([node]
   (when-let [tile ^TileEntity node]
     (world-data/get-connection-by-node
       (world-data/get (.getWorld tile))
       node)))
  ([user]
   (when-let [tile ^TileEntity user]
     (world-data/get-connection-by-user
       (world-data/get (.getWorld tile))
       user))))

(defn is-receiver-linked? [receiver]
  (boolean (get-node-conn receiver)))

(defn is-generator-linked? [generator]
  (boolean (get-node-conn generator)))

(defn get-nodes-in-range [world pos]
  (let [range 20.0]
    (->> (world-data/get-blocks-in-range world (.getX pos) (.getY pos) (.getZ pos) range)
         (filter #(instance? TileEntity %))
         (filter #(satisfies? wireless/IWirelessNode %))
         (filter (fn [node]
                  (let [conn (get-node-conn node)
                        node-pos (.getPos ^TileEntity node)
                        dist-sq (+ (Math/pow (- (.getX pos) (.getX node-pos)) 2)
                                 (Math/pow (- (.getY pos) (.getY node-pos)) 2)
                                 (Math/pow (- (.getZ pos) (.getZ node-pos)) 2))
                        range (.getRange node)]
                    (and (<= dist-sq (* range range))
                         (< (.getLoad conn) (.getCapacity conn)))))))))

(defn create-network! [matrix ssid password]
  (events/create-network! matrix ssid password))

(defn link-node! [node network password]
  (events/link-node! node network password))

(defn unlink-node! [node network]
  (events/unlink-node! node network))

(defn link-user! [user node]
  (events/link-user! user node))

(defn unlink-user! [user]
  (events/unlink-user! user))

(defn destroy-network! [network]
  (events/destroy-network! network))

(defn generator-linked? [generator]
  (has-node? generator))

(defn link-generator! [generator node]
  (link-to-node! generator node))

(defn unlink-generator! [generator]
  (unlink! generator))