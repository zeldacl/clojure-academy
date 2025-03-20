(ns cn.academy.block.multiblock.capabilities
  (:require [cn.academy.block.multiblock.machine-state :as machine])
  (:import [net.minecraftforge.energy IEnergyStorage]))

(defrecord MultiblockEnergyHandler [machine max-transfer]
  IEnergyStorage
  (receiveEnergy [_ maxReceive simulate]
    (let [space-left (- (:max-energy machine)
                       (machine/get-energy machine))
          amount (min maxReceive max-transfer space-left)]
      (when (and (pos? amount) (not simulate))
        (machine/add-energy! machine amount))
      amount))
  
  (extractEnergy [_ maxExtract simulate]
    (let [energy (machine/get-energy machine)
          amount (min maxExtract max-transfer energy)]
      (when (and (pos? amount) (not simulate))
        (machine/use-energy! machine amount))
      amount))
  
  (getEnergyStored [_]
    (machine/get-energy machine))
  
  (getMaxEnergyStored [_]
    (:max-energy machine))
  
  (canExtract [_]
    true)
  
  (canReceive [_]
    true))