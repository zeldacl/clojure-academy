(ns cn.academy.block.matrix-energy
  (:require [cn.academy.block.matrix :as matrix]))

(def ^:const base-capacity 2000.0)
(def ^:const plate-capacity-multiplier 1.5)

(def ^:const max-energy 100000)
(def ^:const max-receive 500)
(def ^:const max-extract 500)

(defprotocol IMatrixEnergy
  (get-capacity [this])
  (get-energy [this])
  (add-energy! [this amount])
  (remove-energy! [this amount])
  (can-receive? [this amount])
  (can-extract? [this amount]))

(defprotocol IEnergyHandler
  (get-stored-energy [this])
  (get-max-energy [this])
  (receive-energy [this amount simulate?])
  (extract-energy [this amount simulate?])
  (can-receive? [this])
  (can-extract? [this])
  (save-to-nbt [this])
  (load-from-nbt! [this nbt]))

(defrecord MatrixEnergy [matrix]
  IMatrixEnergy
  (get-capacity [_]
    (let [plates (matrix/get-plate-count matrix)]
      (* base-capacity (Math/pow plate-capacity-multiplier plates))))
  
  (get-energy [_]  
    (* base-capacity (matrix/get-core-level matrix)))
  
  (add-energy! [this amount]
    (when (can-receive? this amount)
      (let [current (matrix/get-core-level matrix)
            max-level (matrix/get-plate-count matrix)
            new-level (min max-level (+ current (/ amount base-capacity)))]
        (matrix/set-core-level! matrix new-level))))
  
  (remove-energy! [this amount]
    (when (can-extract? this amount)
      (let [current (matrix/get-core-level matrix)
            new-level (max 0 (- current (/ amount base-capacity)))]
        (matrix/set-core-level! matrix new-level))))
  
  (can-receive? [this amount]
    (let [current (get-energy this)
          capacity (get-capacity this)]
      (<= (+ current amount) capacity)))
  
  (can-extract? [this amount]
    (>= (get-energy this) amount)))

(defrecord EnergyHandler [energy]
  IEnergyHandler
  (get-stored-energy [_]
    @energy)
  
  (get-max-energy [_]
    max-energy)
  
  (receive-energy [_ amount simulate?]
    (let [actual (min amount (- max-energy @energy))]
      (when-not simulate?
        (swap! energy + actual))
      actual))
  
  (extract-energy [_ amount simulate?]
    (let [actual (min amount @energy)]
      (when-not simulate?
        (swap! energy - actual))
      actual))
  
  (can-receive? [_] true)
  
  (can-extract? [_] true)
  
  (save-to-nbt [_]
    {"energy" @energy})
  
  (load-from-nbt! [_ nbt]
    (reset! energy (get nbt "energy" 0))))

(defn create-energy-handler [matrix]
  (->MatrixEnergy matrix))

(defn create-energy-handler []
  (->EnergyHandler (atom 0)))