(ns cn.academy.tech-system.energy-system.block.node
  (:require [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.api.wireless :as wireless-api]
            [mcmod.capabilities :as cap]))

(defprotocol IEnergyNode
  (connect-to-network [this])
  (disconnect-from-network [this])
  (is-connected? [this])
  (send-energy [this target amount simulate?])
  (receive-energy [this source amount simulate?]))

(defrecord NodeEnergyStorage [node]
  ;; Energy storage implementation
  cap/IEnergyStorage
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
  
  ;; Wireless node functionality
  IEnergyNode
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