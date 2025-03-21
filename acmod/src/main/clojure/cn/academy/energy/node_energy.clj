(ns cn.academy.energy.node-energy
  (:require [mcmod.protocols :refer :all]
            [mcmod.capabilities :as cap]
            [cn.academy.block.tileentity.tile-node :as tile-node]))

(defprotocol IWirelessEnergy
  (connect-to-network [this])
  (disconnect-from-network [this])
  (is-connected? [this])
  (send-energy [this target amount simulate?])
  (receive-energy [this source amount simulate?]))

;; Energy storage implementation for nodes using mcmod abstractions
(defrecord NodeEnergyStorage [node]
  ;; Implement the IEnergyStorage protocol from mcmod instead of direct Forge import
  IEnergyStorage
  (receive-energy [_ max-receive simulate]
    (let [space (- (.getMaxEnergy node) (.getEnergy node))
          amount (min max-receive space)]
      (when-not simulate
        (.setEnergy node (+ (.getEnergy node) amount)))
      amount))
  
  (extract-energy [_ max-extract simulate]
    (let [amount (min max-extract (.getEnergy node))]
      (when-not simulate
        (.setEnergy node (- (.getEnergy node) amount)))
      amount))
  
  (get-energy-stored [_]
    (int (.getEnergy node)))
  
  (get-max-energy-stored [_]
    (int (.getMaxEnergy node)))
  
  (can-extract? [_]
    true)
  
  (can-receive? [_]
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
                      (do (extract-energy this max-send false)
                          (cap/receive-energy target max-send false))))
          max-send))))
  
  (receive-energy [this source amount simulate?]
    (when (.isEnabled node)
      (let [bandwidth (.getBandwidth node)
            space (- (.getMaxEnergy node) (.getEnergy node))
            max-receive (min amount bandwidth space)]
        (when (pos? max-receive)
          (if simulate?
            max-receive
            (receive-energy this max-receive false)))))))

(defn create-energy-storage [node]
  (->NodeEnergyStorage node))