(ns forge-impl.capability-wrapper
  (:require [mcmod.capabilities :as cap])
  (:import [net.minecraftforge.energy IEnergyStorage]
           [net.minecraft.util Direction]
           [net.minecraftforge.common.capabilities Capability CapabilityInject]))

(defn create-forge-energy-storage [mcmod-storage]
  (reify IEnergyStorage
    (receiveEnergy [this maxReceive simulate]
      (cap/receive-energy mcmod-storage maxReceive simulate))
    
    (extractEnergy [this maxExtract simulate]
      (cap/extract-energy mcmod-storage maxExtract simulate))
    
    (getEnergyStored [this]
      (cap/get-energy-stored mcmod-storage))
    
    (getMaxEnergyStored [this]
      (cap/get-max-energy-stored mcmod-storage))
    
    (canExtract [this]
      true)
    
    (canReceive [this]
      true)))

(defn wrap-capability-provider [mcmod-provider forge-caps]
  (proxy [net.minecraftforge.common.capabilities.ICapabilityProvider] []
    (getCapability [^Capability cap ^Direction side]
      (when (cap/has-capability? mcmod-provider cap side)
        (let [mcmod-cap (cap/get-capability mcmod-provider cap side)]
          (case (.getName cap)
            "forge:energy" (create-forge-energy-storage mcmod-cap)
            nil)))))) 