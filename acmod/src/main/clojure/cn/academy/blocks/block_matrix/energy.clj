(ns cn.academy.blocks.block-matrix.energy
  "Energy management for the wireless matrix block."
  (:require [cn.academy.blocks.block-matrix.protocols :refer [IMatrixEnergy]]
            [cn.academy.blocks.block-matrix.utils :as utils]
            [cn.academy.blocks.block-matrix.inventory :as inventory]
            [mcmod.protocols :refer [IEnergyStorage]]
            [clojure.tools.logging :as log]))

;; Energy tracking implementation
(defrecord MatrixEnergy [config state energy-atom]
  ;; Implement energy protocol for matrix-specific operations
  IMatrixEnergy
  (get-energy [_]
    (:current @energy-atom))
  
  (set-energy [_ amount]
    (let [max-capacity (get-energy-capacity _)]
      (swap! energy-atom assoc :current 
             (max 0 (min amount max-capacity)))))
  
  (add-energy [this amount]
    (let [current (get-energy this)
          max-capacity (get-energy-capacity this)
          space (- max-capacity current)
          added (min amount space)]
      (set-energy this (+ current added))
      added))
  
  (remove-energy [this amount]
    (let [current (get-energy this)
          removed (min amount current)]
      (set-energy this (- current removed))
      removed))
  
  (get-energy-capacity [_]
    (let [core-level (inventory/get-core-level (:core-item state))
          plate-count (inventory/get-plate-count state)]
      (if (and core-level plate-count)
        (utils/calculate-energy-capacity 
          (get-in @config [:energy :base-capacity])
          (get-in @config [:energy :core-multiplier])
          (get-in @config [:energy :plate-multiplier])
          core-level 
          plate-count)
        0)))
  
  (get-transfer-rate [_]
    (let [core-level (inventory/get-core-level (:core-item state))]
      (if core-level
        (utils/calculate-transfer-rate
          (get-in @config [:energy :base-transfer])
          (get-in @config [:energy :core-multiplier])
          core-level)
        0)))
  
  ;; Standard Forge energy capability implementation
  IEnergyStorage
  (getEnergyStored [this]
    (get-energy this))
  
  (getMaxEnergyStored [this]
    (get-energy-capacity this))
  
  (canExtract [this]
    (> (get-energy this) 0))
  
  (canReceive [this]
    (< (get-energy this) (get-energy-capacity this)))
  
  (extractEnergy [this maxExtract simulate]
    (if simulate
      (min maxExtract (get-energy this))
      (remove-energy this maxExtract)))
  
  (receiveEnergy [this maxReceive simulate]
    (if simulate
      (let [current (get-energy this)
            max-capacity (get-energy-capacity this)
            space (- max-capacity current)]
        (min maxReceive space))
      (add-energy this maxReceive))))

;; Energy stats calculation
(defn calculate-consumption
  "Calculate the energy consumption for the matrix based on state"
  [energy network-connections]
  (let [base-consumption (get-in @(:config energy) [:energy :base-consumption])
        multiplier (+ 1.0 (* 0.1 (count network-connections)))]
    (* base-consumption multiplier)))

;; Energy utility functions
(defn get-energy-percentage
  "Get energy stored as a percentage of capacity"
  [energy]
  (let [current (get-energy energy)
        capacity (get-energy-capacity energy)]
    (if (pos? capacity)
      (/ current capacity)
      0.0)))

(defn has-energy-for-operation?
  "Check if the matrix has enough energy for an operation"
  [energy amount]
  (>= (get-energy energy) amount))

(defn consume-operation-energy
  "Try to consume energy for an operation, returns true if successful"
  [energy amount]
  (when (has-energy-for-operation? energy amount)
    (remove-energy energy amount)
    true))

;; Factory function
(defn create-energy
  "Create a new matrix energy manager"
  [config state]
  (->MatrixEnergy config state (atom {:current 0})))

;; Serialization helpers
(defn get-energy-data
  "Get energy data for serialization"
  [energy]
  {:current (get-energy energy)})

(defn load-energy-data!
  "Load energy data from serialized form"
  [energy data]
  (set-energy energy (:current data)))