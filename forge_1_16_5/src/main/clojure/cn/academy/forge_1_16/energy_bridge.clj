(ns cn.academy.forge-1-16.energy-bridge
  (:require [cn.academy.block.matrix-energy :as energy])
  (:import [net.minecraftforge.energy IEnergyStorage]
           [net.minecraftforge.common.capabilities Capability CapabilityInject]
           [net.minecraft.util Direction]))

(defprotocol IForgeEnergyBridge
  "Bridge between platform-independent energy and Forge energy"
  (wrap-forge-energy [this forge-energy]
    "Wrap Forge energy storage with platform-independent interface")
  (to-forge-energy [this energy-storage]
    "Convert platform-independent storage to Forge energy"))

(defrecord ForgeEnergyWrapper [forge-energy]
  energy/IEnergyStorage
  (receive-energy [_ amount simulate]
    (.receiveEnergy forge-energy amount simulate))
  
  (extract-energy [_ amount simulate]
    (.extractEnergy forge-energy amount simulate))
  
  (get-energy-stored [_]
    (.getEnergyStored forge-energy))
  
  (get-energy-capacity [_]
    (.getMaxEnergyStored forge-energy))
  
  (can-receive? [_]
    (.canReceive forge-energy))
  
  (can-extract? [_]
    (.canExtract forge-energy)))

(defrecord ForgeEnergyBridge []
  IForgeEnergyBridge
  (wrap-forge-energy [_ forge-energy]
    (->ForgeEnergyWrapper forge-energy))
  
  (to-forge-energy [_ energy-storage]
    (reify IEnergyStorage
      (receiveEnergy [_ amount simulate]
        (energy/receive-energy energy-storage amount simulate))
      
      (extractEnergy [_ amount simulate]
        (energy/extract-energy energy-storage amount simulate))
      
      (getEnergyStored [_]
        (energy/get-energy-stored energy-storage))
      
      (getMaxEnergyStored [_]
        (energy/get-energy-capacity energy-storage))
      
      (canReceive [_]
        (energy/can-receive? energy-storage))
      
      (canExtract [_]
        (energy/can-extract? energy-storage)))))

(defn create-energy-bridge []
  (->ForgeEnergyBridge))

(defn register-capability [registry]
  (let [energy-cap (CapabilityInject IEnergyStorage)]
    (.register registry energy-cap IEnergyStorage)))