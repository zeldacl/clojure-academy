(ns cn.academy.energy.node-energy
  (:require [mcmod.protocols :refer :all]
            [cn.academy.block.tileentity.tile-node :as tile-node])
  (:import [net.minecraftforge.energy IEnergyStorage]))

(defprotocol IWirelessEnergy
  (connect-to-network [this])
  (disconnect-from-network [this])
  (is-connected? [this])
  (send-energy [this target amount simulate?])
  (receive-energy [this source amount simulate?]))

;; Energy storage implementation for nodes
(defrecord NodeEnergyStorage [node]
  IEnergyStorage
  (receiveEnergy [_ maxReceive simulate]
    (let [space (- (.getMaxEnergy node) (.getEnergy node))
          amount (min maxReceive space)]
      (when-not simulate
        (.setEnergy node (+ (.getEnergy node) amount)))
      amount))
  
  (extractEnergy [_ maxExtract simulate]
    (let [amount (min maxExtract (.getEnergy node))]
      (when-not simulate
        (.setEnergy node (- (.getEnergy node) amount)))
      amount))
  
  (getEnergyStored [_]
    (int (.getEnergy node)))
  
  (getMaxEnergyStored [_]
    (int (.getMaxEnergy node)))
  
  (canExtract [_]
    true)
  
  (canReceive [_]
    true)
  
  IWirelessEnergy
  (connect-to-network [_]
    (.setEnabled node true))
  
  (disconnect-from-network [_]
    (.setEnabled node false))
  
  (is-connected? [_]
    (.isEnabled node))
  
  (send-energy [this target amount simulate?]
    (when (.isEnabled node)
      (let [bandwidth (.getBandwidth node)
            energy (.getEnergy node)
            max-send (min amount bandwidth energy)]
        (when (and (pos? max-send) 
                  (or simulate? 
                      (do (.extractEnergy this max-send false)
                          (.receiveEnergy target max-send false))))
          max-send))))
  
  (receive-energy [this source amount simulate?]
    (when (.isEnabled node)
      (let [bandwidth (.getBandwidth node)
            space (- (.getMaxEnergy node) (.getEnergy node))
            max-receive (min amount bandwidth space)]
        (when (pos? max-receive)
          (if simulate?
            max-receive
            (.receiveEnergy this max-receive false)))))))

(defn create-energy-storage [node]
  (->NodeEnergyStorage node))