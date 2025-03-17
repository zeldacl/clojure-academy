(ns cn.academy.block.matrix-energy-adapter
  (:require [cn.academy.block.matrix-energy :as energy])
  (:import [net.minecraftforge.energy IEnergyStorage]))

(defrecord ForgeEnergyAdapter [matrix-energy]
  IEnergyStorage
  (receiveEnergy [this maxReceive simulate]
    (if (energy/can-receive? matrix-energy maxReceive)
      (let [amount maxReceive]
        (when-not simulate
          (energy/add-energy! matrix-energy amount))
        amount)
      0))
  
  (extractEnergy [this maxExtract simulate]
    (if (energy/can-extract? matrix-energy maxExtract)
      (let [amount maxExtract]
        (when-not simulate
          (energy/remove-energy! matrix-energy amount))
        amount)
      0))
  
  (getEnergyStored [_]
    (energy/get-energy matrix-energy))
  
  (getMaxEnergyStored [_]
    (energy/get-capacity matrix-energy))
  
  (canExtract [_]
    true)
  
  (canReceive [_]
    true))

(defn create-adapter [matrix-energy]
  (->ForgeEnergyAdapter matrix-energy))