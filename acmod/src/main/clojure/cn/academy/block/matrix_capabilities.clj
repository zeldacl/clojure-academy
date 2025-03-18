(ns cn.academy.block.matrix-capabilities
  (:require [cn.academy.capability.core :as cap]
            [cn.academy.block.matrix :as matrix]))

(defrecord MatrixEnergyHandler [matrix]
  cap/ICapabilityHandler
  (handle [_ operation & args]
    (case operation
      :get-energy (matrix/get-energy-stored matrix)
      :get-capacity (matrix/get-energy-capacity matrix)
      :receive-energy (matrix/receive-energy matrix (first args))
      :extract-energy (matrix/extract-energy matrix (first args))
      nil))

  cap/ICapabilityType
  (get-type-id [_] :energy))

(defrecord MatrixInventoryHandler [matrix]
  cap/ICapabilityHandler
  (handle [_ operation & args]
    (case operation
      :get-slots (matrix/get-slots matrix)
      :get-stack (matrix/get-stack matrix (first args))
      :set-stack (matrix/set-stack matrix (first args) (second args))
      :is-valid (matrix/is-valid-slot? matrix (first args) (second args))
      nil))

  cap/ICapabilityType
  (get-type-id [_] :inventory))

(defrecord MatrixCapabilityProvider [matrix]
  cap/ICapabilityProvider
  (get-capabilities [_]
    {:energy (->MatrixEnergyHandler matrix)
     :inventory (->MatrixInventoryHandler matrix)})
  
  (get-capability [this type side]
    (get (cap/get-capabilities this) type))
  
  (invalidate-capabilities! [_]
    nil))

(defn create-provider [matrix]
  (->MatrixCapabilityProvider matrix))