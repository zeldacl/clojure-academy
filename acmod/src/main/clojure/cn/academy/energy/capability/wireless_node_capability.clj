(ns cn.academy.energy.capability.wireless-node-capability
  (:require [cn.academy.energy.api.wireless :as wireless]
            [mcmod.capabilities :as cap]
            [mcmod.nbt :as nbt]))

(def ^:private WIRELESS-NODE-CAPABILITY (atom nil))

(defrecord WirelessNodeStorage [energy max-energy range max-connections]
  wireless/IWirelessNode
  (get-range [_] @range)
  
  (get-max-connections [_] @max-connections)
  
  (get-max-energy [_] @max-energy)
  
  (get-energy [_] @energy)
  
  (connect [this other]
    (when (wireless/can-connect? this other)
      (swap! energy #(min (+ % (wireless/get-energy other)) @max-energy))
      true))
  
  (disconnect [_ _]
    (reset! energy 0.0))
  
  (can-connect? [_ other]
    (and (wireless/is-wireless-node? other)
         (< @energy @max-energy)))
  
  cap/INBTSerializable
  (write-nbt [_]
    (doto (nbt/create-compound)
      (nbt/put-double "energy" @energy)
      (nbt/put-double "maxEnergy" @max-energy)
      (nbt/put-double "range" @range)
      (nbt/put-int "maxConnections" @max-connections)))
  
  (read-nbt [_ nbt]
    (reset! energy (nbt/get-double nbt "energy"))
    (reset! max-energy (nbt/get-double nbt "maxEnergy"))
    (reset! range (nbt/get-double nbt "range"))
    (reset! max-connections (nbt/get-int nbt "maxConnections"))))

(defn create-storage [max-energy range max-connections]
  (->WirelessNodeStorage 
    (atom 0.0)
    (atom max-energy)
    (atom range)
    (atom max-connections)))

(defn register! []
  (cap/register-capability 
    wireless/IWirelessNode
    (reify cap/ICapabilityStorage
      (write-nbt [_ capability instance side]
        (cap/write-nbt instance))
      (read-nbt [_ capability instance side nbt]
        (cap/read-nbt instance nbt)))
    #(create-storage 100000.0 20.0 4))
  
  (reset! WIRELESS-NODE-CAPABILITY
    (cap/get-capability-type wireless/IWirelessNode)))

(defn get-capability []
  @WIRELESS-NODE-CAPABILITY)