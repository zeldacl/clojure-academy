(ns cn.academy.block.matrix-energy
  (:require [cn.academy.block.matrix-state :as state]
            [cn.academy.block.matrix-config :as config]))

(defprotocol IEnergyStorage
  "Protocol for energy storage capabilities"
  (receive-energy [this amount simulate]
    "Receive energy into storage")
  (extract-energy [this amount simulate]
    "Extract energy from storage")
  (get-energy-stored [this]
    "Get current stored energy")
  (get-energy-capacity [this]
    "Get maximum energy capacity")
  (can-receive? [this]
    "Check if can receive energy")
  (can-extract? [this]
    "Check if can extract energy"))

(defprotocol IEnergyNetwork
  "Protocol for matrix energy networking"
  (connect-node [this node]
    "Connect energy node to network")
  (disconnect-node [this node]
    "Disconnect energy node from network")
  (get-connected-nodes [this]
    "Get all connected nodes")
  (transfer-energy [this target amount]
    "Transfer energy to target")
  (get-bandwidth [this]
    "Get current bandwidth"))

(defrecord MatrixEnergy [state config energy-atom]
  IEnergyStorage
  (receive-energy [this amount simulate]
    (when (state/is-formed? state)
      (let [capacity (get-energy-capacity this)
            stored (get-energy-stored this)
            space (- capacity stored)
            accept-amount (min amount space)]
        (when (pos? accept-amount)
          (when-not simulate
            (swap! energy-atom update :stored + accept-amount))
          accept-amount))))
  
  (extract-energy [this amount simulate]
    (when (state/is-formed? state)
      (let [stored (get-energy-stored this)
            extract-amount (min amount stored)]
        (when (pos? extract-amount)
          (when-not simulate
            (swap! energy-atom update :stored - extract-amount))
          extract-amount))))
  
  (get-energy-stored [_]
    (:stored @energy-atom))
  
  (get-energy-capacity [_]
    (* (state/get-core-level state)
       (config/get-capacity-multiplier config)))
  
  (can-receive? [_]
    (state/is-formed? state))
  
  (can-extract? [_]
    (state/is-formed? state))

  IEnergyNetwork  
  (connect-node [_ node]
    (swap! energy-atom update :nodes conj node))
  
  (disconnect-node [_ node]
    (swap! energy-atom update :nodes disj node))
  
  (get-connected-nodes [_]
    (:nodes @energy-atom))
  
  (transfer-energy [this target amount]
    (let [bandwidth (get-bandwidth this)
          efficiency (config/get-transfer-efficiency config)
          transfer-amount (min amount bandwidth)
          actual-amount (extract-energy this transfer-amount true)]
      (when (pos? actual-amount)
        (let [received (receive-energy target (* actual-amount efficiency) false)]
          (when (pos? received)
            (extract-energy this (/ received efficiency) false))))))
  
  (get-bandwidth [_]
    (* (Math/pow (state/get-core-level state) 2)
       (config/get-bandwidth-multiplier config))))

(defn create-matrix-energy [state config]
  (->MatrixEnergy 
    state 
    config
    (atom {:stored 0.0
           :nodes #{}})))

(defn update-energy! [energy]
  (let [stored (get-energy-stored energy)
        base-consumption (config/get-base-consumption (:config energy))]
    (when (>= stored base-consumption)
      (extract-energy energy base-consumption false))))