(ns cn.li.bridge.matrix.capability
  (:require [cn.li.bridge.matrix.api :as matrix]
            [cn.li.bridge.matrix.energy :as energy])
  (:import [net.minecraftforge.common.capabilities Capability ICapabilityProvider]
           [net.minecraftforge.energy CapabilityEnergy IEnergyStorage]
           [net.minecraftforge.common.util LazyOptional]
           [net.minecraft.util Direction]))

(defrecord MatrixCapabilityProvider [matrix energy-storage energy-optional]
  ICapabilityProvider
  (getCapability [_ capability side]
    (if (= capability (CapabilityEnergy/ENERGY))
      @energy-optional
      LazyOptional/EMPTY)))

(defn create-capability-provider [matrix]
  (let [energy-storage (energy/create-energy-storage matrix)
        energy-optional (atom (LazyOptional/of #(identity energy-storage)))]
    (->MatrixCapabilityProvider 
      matrix 
      energy-storage 
      energy-optional)))

(defn register-capabilities [registry]
  ; Register the energy storage capability
  (.register registry 
            (CapabilityEnergy/ENERGY) 
            IEnergyStorage))