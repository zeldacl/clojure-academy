(ns cn.academy.tech-system.energy-system.capability.wireless
  (:require [cn.academy.tech-system.energy-system.api :as energy-api]
            [mcmod.protocols :refer [ITileEntity IInventory]]
            [mcmod.capabilities :as cap]
            [mcmod.nbt :as nbt]))

(defprotocol IWirelessNode
  "Protocol for wireless node functionality"
  (get-node-type [this] "Get the node type")
  (get-energy [this] "Get current energy")
  (get-max-energy [this] "Get maximum energy capacity")
  (get-bandwidth [this] "Get energy transfer rate")
  (get-range [this] "Get wireless range")
  (get-capacity [this] "Get connection capacity")
  (connect [this other] "Connect to another node")
  (disconnect [this other] "Disconnect from another node")
  (can-connect? [this other] "Check if can connect"))

(defrecord WirelessNodeCapability [energy max-energy range max-connections]
  IWirelessNode
  (get-node-type [_] :wireless)
  
  (get-energy [_] @energy)
  
  (get-max-energy [_] @max-energy)
  
  (get-bandwidth [_] 1000.0)
  
  (get-range [_] @range)
  
  (get-capacity [_] @max-connections)
  
  (connect [this other]
    (when (can-connect? this other)
      (energy-api/transfer-energy this other (get-energy other) false)))
  
  (disconnect [_ _]
    (reset! energy 0.0))
  
  (can-connect? [_ other]
    (and (satisfies? IWirelessNode other)
         (< (get-energy other) (get-max-energy other))))

  cap/IEnergyStorage  
  (receive-energy [_ amount simulate]
    (let [current @energy
          max-receive 1000.0
          space (- @max-energy current)
          actual-receive (min amount space max-receive)]
      (when (and (pos? actual-receive) (not simulate))
        (swap! energy + actual-receive))
      actual-receive))
  
  (extract-energy [_ amount simulate]
    (let [current @energy
          max-extract 1000.0
          actual-extract (min amount current max-extract)]
      (when (and (pos? actual-extract) (not simulate))
        (swap! energy - actual-extract))
      actual-extract))
  
  (get-energy-stored [_]
    @energy)
  
  (get-max-energy-stored [_]
    @max-energy))

(defn create-capability [max-energy range max-connections]
  (->WirelessNodeCapability 
    (atom 0.0)
    (atom max-energy)
    (atom range)
    (atom max-connections)))