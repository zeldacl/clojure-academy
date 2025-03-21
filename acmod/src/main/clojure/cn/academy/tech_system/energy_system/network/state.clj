(ns cn.academy.tech-system.energy-system.network.state
  (:require [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.persistence :as persistence]
            [clojure.tools.logging :as log]))

(def ^:private world-networks (atom {}))

(defn get-network
  "Get a network by ID"
  [network-id]
  (get @world-networks network-id))

(defn get-all-networks []
  @world-networks)

(defn register-network!
  "Register a new wireless network"
  [network-id props]
  (swap! world-networks assoc network-id props))

(defn remove-network!
  "Remove a wireless network"
  [network-id]
  (swap! world-networks dissoc network-id))

(defn get-node-network
  "Get the network ID a node belongs to"
  [node-id]
  (->> (vals @world-networks)
       (filter #(contains? (:nodes %) node-id))
       first
       :id))

(defn add-node!
  "Add a node to a network"
  [network-id node-id]
  (swap! world-networks update-in [network-id :nodes] conj node-id))

(defn remove-node!
  "Remove a node from a network"
  [network-id node-id]
  (swap! world-networks update-in [network-id :nodes] disj node-id))

(defn get-network-nodes
  "Get all node IDs in a network"
  [network-id]
  (get-in @world-networks [network-id :nodes]))

(defn save-state!
  "Save network state to disk"
  [world]
  (persistence/save-all! world @world-networks))

(defn load-state!
  "Load network state from disk"
  [world]
  (when-let [data (persistence/load-all! world)]
    (reset! world-networks data)))

(defn init! []
  ;; Register world load/save handlers
  (persistence/register-handlers!
    {:save-networks save-state!
     :load-networks load-state!})
  (log/info "Network state system initialized"))