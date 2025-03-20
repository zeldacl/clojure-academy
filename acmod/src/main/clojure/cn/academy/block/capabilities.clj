(ns cn.academy.block.capabilities
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core]
            [clojure.tools.logging :as log]))

(defrecord WirelessNetworkCapability [node]
  IWirelessNode
  (get-range [this]
    (get-range node))
    
  (get-max-connections [this]
    (get-max-connections node))
    
  (get-max-energy [this]
    (get-max-energy-stored node))
    
  (get-energy [this]
    (get-energy-stored node))
    
  (connect [this other]
    (when (can-connect? node other)
      (connect node other)))
      
  (disconnect [this other]
    (disconnect node other))
    
  (can-connect? [this other]
    (can-connect? node other))

  IEnergyStorage
  (receive-energy [this amount simulate]
    (receive-energy node amount simulate))
    
  (extract-energy [this amount simulate]
    (extract-energy node amount simulate))
    
  (get-energy-stored [this]
    (get-energy-stored node))
    
  (get-max-energy-stored [this]
    (get-max-energy-stored node))
    
  (can-receive? [this]
    true)
    
  (can-extract? [this]
    true))

(defn create-wireless-capability [node]
  (->WirelessNetworkCapability node))

;; Factory functions for creating block capabilities

(defn add-energy-capability [provider max-energy]
  (let [storage (create-energy-storage max-energy)]
    (assoc provider :energy-storage storage)))

(defn add-inventory-capability [provider inventory slots]
  (let [handler (create-item-handler inventory slots)]
    (assoc provider :item-handler handler)))

(defn add-wireless-capability [provider node]
  (let [wireless (create-wireless-capability node)]
    (assoc provider :wireless wireless)))