(ns cn.academy.tech-system.energy-system.api
  "Public API for the Academy Mod Energy System"
  (:require [cn.academy.tech-system.energy-system.core :as core]
            [cn.academy.tech-system.energy-system.network.wireless :as wireless]
            [cn.academy.tech-system.energy-system.config :as config]
            [cn.academy.tech-system.energy-system.registry :as registry]))

(defn create-energy-storage
  "Create a new energy storage with specified capacity and transfer rates.
   
   Parameters:
   - capacity: Maximum energy storage capacity in FE
   - max-receive: Maximum energy receive rate per tick
   - max-extract: Maximum energy extraction rate per tick
   
   Returns: An energy storage capability instance"
  [& {:keys [capacity max-receive max-extract]
      :or {capacity 10000
           max-receive 1000
           max-extract 1000}}]
  (core/create-storage 
    :capacity capacity
    :max-receive max-receive
    :max-extract max-extract))

(defn find-energy-receivers
  "Find all energy-capable tile entities within range of a position.
   
   Parameters:
   - world: The world instance
   - pos: Position map with :x :y :z keys
   - range: Search radius in blocks
   
   Returns: Sequence of tile entities that can receive energy"
  [world pos range]
  (wireless/find-nearby-receivers world pos range))

(defn transfer-energy
  "Transfer energy between two tile entities.
   
   Parameters:
   - source: Source tile entity with energy capability
   - target: Target tile entity with energy capability
   - amount: Maximum amount of energy to transfer
   
   Returns: Amount of energy actually transferred, or nil if transfer failed"
  [source target amount]
  (core/transfer-energy source target amount))

(defn get-config
  "Get energy system configuration values.
   
   Returns: Map of configuration values"
  []
  (config/get-wireless-config))

(def ^:dynamic *energy-impl* nil)

(defprotocol IEnergyCapability
  (get-stored-energy [this])
  (get-max-energy [this])
  (receive-energy [this amount simulate]))

(defn create-energy-storage [max-energy get-energy-fn set-energy-fn]
  (reify IEnergyCapability
    (get-stored-energy [_]
      (get-energy-fn))
    
    (get-max-energy [_]
      max-energy)
    
    (receive-energy [_ amount simulate]
      (let [current (get-energy-fn)
            new-energy (min (+ current amount) max-energy)
            accepted (- new-energy current)]
        (when-not simulate
          (set-energy-fn new-energy))
        accepted))))

(defn- get-nearby-chunk-caches [world pos]
  (let [range (config/get-config [:cat-engine :wireless-range] 16)
        chunk-x (bit-shift-right (.getX pos) 4)
        chunk-z (bit-shift-right (.getZ pos) 4)
        radius (-> range inc (bit-shift-right 4))]
    (for [dx (range (- radius) (inc radius))
          dz (range (- radius) (inc radius))
          :let [cx (+ chunk-x dx)
                cz (+ chunk-z dz)]]
      (chunk-cache/get-or-create-cache world cx cz))))

(defn get-nearby-nodes [world pos]
  (let [range (config/get-config [:cat-engine :wireless-range] 16)
        allow-interdim (config/get-config [:cat-engine :allow-interdimensional] true)
        caches (get-nearby-chunk-caches world pos)
        nodes (mapcat chunk-cache/get-nodes caches)]
    (if allow-interdim
      nodes
      (filter #(= (.dimension world) (.dimension (.getWorld %))) nodes))))

;; Network-related API functions to provide a clean interface for the block modules
(defn get-network
  "Get a network by ID"
  [network-id]
  (distribution/get-network network-id))

(defn get-network-nodes
  "Get all nodes for a given network"
  [network-id]
  (distribution/get-network-nodes network-id))

(defn create-network!
  "Create a new network and return its ID"
  []
  (let [network (distribution/create-network!)]
    (:id network)))

(defn join-network!
  "Join a node to a network"
  [node-id network-id]
  (when-let [network (get-network network-id)]
    (let [node-network (distribution/get-node-network node-id)]
      (when (and node-network (not= (:id node-network) network-id))
        (distribution/leave-network! node-id))
      (distribution/join-network! node-id network-id))))

(defn leave-network!
  "Remove a node from its network"
  [node-id]
  (distribution/leave-network! node-id))

(defn register-energy-node-type!
  "Register a new energy node type with the system"
  [type-id properties]
  (let [registry (requiring-resolve 'cn.academy.tech-system.energy-system.registry/register-node-type!)]
    (registry type-id properties)))

(defn set-energy-impl! [impl]
  (alter-var-root #'*energy-impl* (constantly impl)))