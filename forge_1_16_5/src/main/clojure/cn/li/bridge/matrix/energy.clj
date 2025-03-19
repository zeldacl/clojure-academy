(ns cn.li.bridge.matrix.energy
  (:require [cn.li.bridge.energy.api :as energy]
            [cn.li.bridge.matrix.api :as matrix])
  (:import [net.minecraftforge.energy IEnergyStorage]
           [net.minecraft.util Direction]))

(def ^:private DEFAULT_CAPACITY 100000)
(def ^:private DEFAULT_TRANSFER 1000)
(def ^:private CORE_MULTIPLIER 2.0)
(def ^:private PLATE_MULTIPLIER 1.5)

(defrecord MatrixEnergyStorage [matrix]
  energy/IEnergyStorage
  (receive-energy [_ amount simulate]
    (matrix/receive-energy matrix amount simulate))
  
  (extract-energy [_ amount simulate]
    (matrix/extract-energy matrix amount simulate))
  
  (get-energy-stored [_]
    (matrix/get-energy-stored matrix))
  
  (get-energy-capacity [_]
    (matrix/get-energy-capacity matrix))
  
  (can-receive? [_]
    (matrix/is-formed? matrix))
  
  (can-extract? [_]
    (matrix/is-formed? matrix)))

(defn calculate-capacity [core-level plate-count]
  (* DEFAULT_CAPACITY 
     (Math/pow CORE_MULTIPLIER core-level)
     (Math/pow PLATE_MULTIPLIER plate-count)))

(defn calculate-transfer-rate [core-level]
  (* DEFAULT_TRANSFER (Math/pow CORE_MULTIPLIER core-level)))

(defn create-energy-storage [matrix]
  (->MatrixEnergyStorage matrix))