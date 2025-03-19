(ns mcmod.capabilities
  (:require [mcmod.protocols :refer :all]))

(defprotocol ICapabilityProvider
  "Protocol for capability providers"
  (has-capability? [this capability-type side] "Check if provider has capability")
  (get-capability [this capability-type side] "Get capability instance")
  (invalidate-capabilities [this] "Invalidate all capabilities"))

(defprotocol IEnergyStorage
  "Energy storage capability"
  (receive-energy [this max-receive simulate] "Receive energy")
  (extract-energy [this max-extract simulate] "Extract energy")
  (get-energy-stored [this] "Get stored energy")
  (get-max-energy-stored [this] "Get max energy capacity"))

(defrecord BasicEnergyStorage [capacity max-receive max-extract]
  IEnergyStorage
  (receive-energy [this max-receive simulate]
    (let [energy-stored (get-energy-stored this)
          accept-amount (min max-receive 
                           (- (get-max-energy-stored this) 
                              energy-stored))]
      (when-not simulate
        (swap! (:energy this) + accept-amount))
      accept-amount))
  
  (extract-energy [this max-extract simulate]
    (let [energy-stored (get-energy-stored this)
          extract-amount (min max-extract energy-stored)]
      (when-not simulate
        (swap! (:energy this) - extract-amount))
      extract-amount))
  
  (get-energy-stored [this]
    @(:energy this))
  
  (get-max-energy-stored [this]
    capacity))

(defn create-energy-storage
  "Create a basic energy storage capability"
  [& {:keys [capacity max-receive max-extract]
      :or {capacity 10000
           max-receive 1000
           max-extract 1000}}]
  (->BasicEnergyStorage capacity max-receive max-extract
                        {:energy (atom 0)}))