(ns cn.academy.blocks.block_matrix.energy
  (:require [mcmod.protocols :refer [IEnergyStorage]]
            [cn.academy.tech-system.energy-system.api :as energy-api]))

(def ^:private DEFAULT_CAPACITY 100000)
(def ^:private DEFAULT_TRANSFER 1000)
(def ^:private CORE_MULTIPLIER 2.0)
(def ^:private PLATE_MULTIPLIER 1.5)

;; Use the standard IEnergyStorage protocol instead of a custom one
(defrecord MatrixEnergy [matrix state energy-atom]
  IEnergyStorage
  (receive-energy [this amount simulate]
    (when (:formed? @state)
      (let [capacity (get-max-energy-stored this)
            stored (get-energy-stored this)
            space (- capacity stored)
            accept-amount (min amount space)]
        (when (pos? accept-amount)
          (when-not simulate
            (swap! energy-atom update :stored + accept-amount))
          accept-amount))))
  
  (extract-energy [this amount simulate]
    (when (:formed? @state)
      (let [stored (get-energy-stored this)
            extract-amount (min amount stored)]
        (when (pos? extract-amount)
          (when-not simulate
            (swap! energy-atom update :stored - extract-amount))
          extract-amount))))
  
  (get-energy-stored [_]
    (:stored @energy-atom))
  
  (get-max-energy-stored [this]
    (let [core-level (:core-level @state)
          plate-count (:plate-count @state)]
      (* DEFAULT_CAPACITY 
         (Math/pow CORE_MULTIPLIER core-level)
         (Math/pow PLATE_MULTIPLIER plate-count))))
  
  (can-receive? [_]
    (:formed? @state))
  
  (can-extract? [_]
    (:formed? @state)))

;; Additional matrix-specific energy functionality that extends the standard interface
(defprotocol IMatrixEnergyExtension
  (get-bandwidth [this]))

(extend-type MatrixEnergy
  IMatrixEnergyExtension
  (get-bandwidth [this]
    (* DEFAULT_TRANSFER 
       (Math/pow CORE_MULTIPLIER (:core-level @(:state this))))))

(defn create-energy
  "Create new matrix energy handler"
  [matrix state]
  (->MatrixEnergy matrix
                  state
                  (atom {:stored 0})))