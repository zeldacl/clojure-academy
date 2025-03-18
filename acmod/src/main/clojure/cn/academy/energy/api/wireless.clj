(ns cn.academy.energy.api.wireless
  (:require [cn.academy.api.energy :as energy])
  (:import [net.minecraftforge.eventbus.api Event]))

(defprotocol IWirelessNode
  "Interface for blocks that can act as wireless network nodes"
  (get-range [this] "Get the maximum connection range of this node")
  (get-max-connections [this] "Get the maximum number of connections this node supports")
  (get-max-energy [this] "Get the maximum energy this node can store")
  (get-energy [this] "Get the current energy stored in this node")
  (set-energy! [this amount] "Set the current energy stored in this node")
  (get-bandwidth [this] "Get the energy transfer rate per tick")
  (connect [this other] "Connect this node to another node")
  (disconnect [this other] "Disconnect this node from another node")
  (can-connect? [this other] "Check if this node can connect to another node"))

(defprotocol IWirelessMatrix  
  "Interface for blocks that initialize and manage wireless networks"
  (create-network [this] "Create a new wireless network")
  (get-network [this] "Get the current network this matrix manages")
  (get-network-name [this] "Get the display name of this network")
  (set-network-name! [this name] "Set the display name of this network")
  (get-range [this] "Get the matrix range for node connections")
  (get-capacity [this] "Get maximum number of nodes this matrix can manage")
  (get-bandwidth [this] "Get the matrix energy transfer bandwidth"))

(defprotocol IWirelessGenerator
  "Interface for blocks that generate energy for the network"
  (generate-energy [this amount] "Generate and add energy to the network")
  (get-generation-rate [this] "Get the energy generation rate per tick"))

(defprotocol IWirelessReceiver
  "Interface for blocks that receive energy from the network"
  (receive-energy [this amount simulate?] "Receive energy from the network, returns amount accepted")
  (get-energy-needed [this] "Get how much energy this receiver needs"))

(defn is-wireless-node? [obj]
  (satisfies? IWirelessNode obj))

(defn is-wireless-matrix? [obj]
  (satisfies? IWirelessMatrix obj))

(defn is-wireless-generator? [obj]
  (satisfies? IWirelessGenerator obj))

(defn is-wireless-receiver? [obj]
  (satisfies? IWirelessReceiver obj))

(defn is-wireless-user? [obj]
  (or (is-wireless-generator? obj)
      (is-wireless-receiver? obj)))

(defn post-event! [event]
  (.post (MinecraftForge/EVENT_BUS) event))

(defn create-matrix-handler [matrix]
  (reify IWirelessMatrix
    (create-network [_] 
      (get-network matrix))
    
    (get-network [_]
      (.getNetwork matrix))
    
    (get-network-name [_]
      (.getNetworkName matrix))
    
    (set-network-name! [_ name]
      (.setNetworkName matrix name))
    
    (get-range [_]
      (.getRange matrix))
    
    (get-capacity [_]
      (.getCapacity matrix))
    
    (get-bandwidth [_]
      (.getBandwidth matrix))))