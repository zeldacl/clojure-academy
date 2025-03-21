(ns cn.academy.tech-system.energy-system.network.wireless
  (:require [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.api :as energy-api]
            [clojure.tools.logging :as log]
            [mcmod.nbt :as nbt]))

(def ^:private network-state
  (atom {:networks {}
         :node-mappings {}}))

(defprotocol IWirelessNetwork
  (add-node! [this node] "Add a node to the network")
  (remove-node! [this node] "Remove a node from the network")
  (get-nodes [this] "Get all nodes in the network")
  (balance-energy! [this] "Balance energy across network"))

(defrecord WirelessNetwork [id state-atom]
  IWirelessNetwork
  (add-node! [_ node]
    (when (satisfies? wireless/IWirelessNode node)
      (let [existing-net-id (get-in @network-state [:node-mappings (:id node)])]
        (when (and existing-net-id (not= existing-net-id id))
          (when-let [existing-net (get-in @network-state [:networks existing-net-id])]
            (remove-node! existing-net node))))
      (swap! state-atom update :nodes conj node)
      (swap! network-state assoc-in [:node-mappings (:id node)] id)))
  
  (remove-node! [_ node]
    (swap! state-atom update :nodes disj node)
    (swap! network-state update :node-mappings dissoc (:id node)))
  
  (get-nodes [_]
    (:nodes @state-atom))
  
  (balance-energy! [this]
    (let [nodes (get-nodes this)
          total-energy (reduce + (map wireless/get-energy nodes))
          target-energy (/ total-energy (count nodes))]
      (doseq [node nodes]
        (let [current (wireless/get-energy node)
              diff (- target-energy current)]
          (if (pos? diff)
            (wireless/receive-energy node diff false)
            (wireless/extract-energy node (- diff) false)))))))

(defn create-network! []
  "Create a new wireless network"
  (let [id (str (random-uuid))
        network (->WirelessNetwork id (atom {:nodes #{}}))]
    (swap! network-state assoc-in [:networks id] network)
    network))

(defn get-network [id]
  "Get a network by ID"
  (get-in @network-state [:networks id]))

(defn get-node-network [node-id]
  "Get the network a node belongs to"
  (when-let [network-id (get-in @network-state [:node-mappings node-id])]
    (get-network network-id)))

(defn merge-networks! [network1 network2]
  "Merge two networks together"
  (let [nodes (get-nodes network2)]
    (doseq [node nodes]
      (add-node! network1 node))
    (swap! network-state update :networks dissoc (:id network2))))

(defn- update-networks! []
  "Update and validate all networks"
  (doseq [[id network] (:networks @network-state)]
    (try
      (balance-energy! network)
      (catch Exception e
        (log/error e "Error updating network" id)))))