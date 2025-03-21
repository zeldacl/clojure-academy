(ns cn.academy.energy.api.wireless-helper
  (:require [cn.academy.api.energy :as energy]
            [cn.academy.energy.impl.wireless-world-data :as world-data]
            [cn.academy.energy.impl.node-connection :as node-conn]
            [cn.academy.energy.api.wireless-events :as events]
            [mcmod.tile-entity :as tile]
            [mcmod.world :as world]
            [mcmod.position :as position]))

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
   (when-let [tile-entity matrix]
     (world-data/get-network-by-matrix 
       (world-data/get (tile/get-world tile-entity))
       matrix)))
  ([node]
   (when-let [tile-entity node]
     (world-data/get-network-by-node
       (world-data/get (tile/get-world tile-entity))
       node))))

(defn is-node-linked? [node]
  (boolean (get-wireless-net node)))

(defn is-matrix-active? [matrix]
  (boolean (get-wireless-net matrix)))

(defn get-node-conn
  "Get the node connection for a node or user"
  ([node]
   (when-let [tile-entity node]
     (world-data/get-connection-by-node
       (world-data/get (tile/get-world tile-entity))
       node)))
  ([user]
   (when-let [tile-entity user]
     (world-data/get-connection-by-user
       (world-data/get (tile/get-world tile-entity))
       user))))

(defn is-receiver-linked? [receiver]
  (boolean (get-node-conn receiver)))

(defn is-generator-linked? [generator]
  (boolean (get-node-conn generator)))

(defn get-nodes-in-range [world pos]
  (let [range 20.0]
    (->> (world-data/get-blocks-in-range world (:x pos) (:y pos) (:z pos) range)
         (filter #(tile/is-tile-entity? %))
         (filter #(satisfies? wireless/IWirelessNode %))
         (filter (fn [node]
                  (let [conn (get-node-conn node)
                        node-pos (tile/get-position node)
                        dist-sq (+ (Math/pow (- (:x pos) (:x node-pos)) 2)
                                 (Math/pow (- (:y pos) (:y node-pos)) 2)
                                 (Math/pow (- (:z pos) (:z node-pos)) 2))
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