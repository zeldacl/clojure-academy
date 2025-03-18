(ns cn.academy.forge-1-12.energy-adapter
  (:require [cn.academy.energy.energy-adapter :as energy])
  (:import [net.minecraftforge.energy IEnergyStorage]))

(defrecord ForgeEnergyAdapter [adapter]
  IEnergyStorage
  (receiveEnergy [_ maxReceive simulate]
    (energy/receive-energy adapter maxReceive simulate))
  
  (extractEnergy [_ maxExtract simulate]
    (energy/extract-energy adapter maxExtract simulate))
  
  (getEnergyStored [_]
    (energy/get-stored-energy adapter))
  
  (getMaxEnergyStored [_]
    (energy/get-max-energy adapter))
  
  (canExtract [_]
    (energy/can-extract adapter))
  
  (canReceive [_]
    (energy/can-receive adapter)))

(defn create-adapter [energy-handler]
  (->ForgeEnergyAdapter (energy/create-adapter energy-handler)))