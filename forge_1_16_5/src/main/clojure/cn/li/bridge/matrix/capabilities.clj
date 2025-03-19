(ns cn.li.bridge.matrix.capabilities
  (:require [cn.li.bridge.matrix.api :as matrix]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.common.capabilities Capability CapabilityInject ICapabilityProvider]
           [net.minecraftforge.energy IEnergyStorage]
           [net.minecraftforge.items IItemHandler]
           [net.minecraft.util Direction LazyOptional]
           [net.minecraft.nbt CompoundNBT]))

(def ^:private energy-capability
  (CapabilityInject/
    (reify IEnergyStorage
      (receiveEnergy [_ maxReceive simulate]
        (matrix/receive-energy matrix maxReceive simulate))
      (extractEnergy [_ maxExtract simulate]
        (matrix/extract-energy matrix maxExtract simulate))
      (getEnergyStored [_]
        (matrix/get-energy-stored matrix))
      (getMaxEnergyStored [_]
        (matrix/get-energy-capacity matrix))
      (canExtract [_]
        (matrix/can-extract? matrix))
      (canReceive [_]
        (matrix/can-receive? matrix)))))

(defrecord MatrixCapabilityProvider [matrix]
  ICapabilityProvider
  (getCapability [_ capability side]
    (cond
      (= capability energy-capability)
      (LazyOptional/of #(reify IEnergyStorage
                          (receiveEnergy [_ maxReceive simulate]
                            (matrix/receive-energy matrix maxReceive simulate))
                          (extractEnergy [_ maxExtract simulate]
                            (matrix/extract-energy matrix maxExtract simulate))
                          (getEnergyStored [_]
                            (matrix/get-energy-stored matrix))
                          (getMaxEnergyStored [_]
                            (matrix/get-energy-capacity matrix))
                          (canExtract [_]
                            (matrix/can-extract? matrix))
                          (canReceive [_]
                            (matrix/can-receive? matrix))))
      
      :else LazyOptional/EMPTY)))

(defn create-capability-provider [matrix]
  (->MatrixCapabilityProvider matrix))

(defn serialize-capabilities [provider ^CompoundNBT tag]
  (let [matrix (.matrix provider)]
    (doto tag
      (.putInt "Energy" (matrix/get-energy-stored matrix))
      (.putString "State" (name (matrix/get-state matrix))))))

(defn deserialize-capabilities [provider ^CompoundNBT tag]
  (let [matrix (.matrix provider)
        energy (.getInt tag "Energy")
        state (keyword (.getString tag "State"))]
    (matrix/handle-sync matrix {:energy energy :state state})))