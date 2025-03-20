(ns forge-impl.energy.energy-adapter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraftforge.energy IEnergyStorage]
           [net.minecraftforge.common.capabilities Capability
                                                 CapabilityInject
                                                 CapabilityManager
                                                 ICapabilityProvider]
           [net.minecraft.util Direction]))

;; Energy capability field
(def ^:private ENERGY_CAPABILITY (atom nil))

;; Energy storage adapter
(defrecord ForgeEnergyStorage [energy-handler]
  IEnergyStorage
  (receiveEnergy [_ maxReceive simulate]
    (receive-energy energy-handler maxReceive simulate))
  
  (extractEnergy [_ maxExtract simulate]
    (extract-energy energy-handler maxExtract simulate))
  
  (getEnergyStored [_]
    (get-energy-stored energy-handler))
  
  (getMaxEnergyStored [_]
    (get-max-energy-stored energy-handler))
  
  (canExtract [_]
    (can-extract? energy-handler))
  
  (canReceive [_]
    (can-receive? energy-handler)))

;; Create energy capability provider
(defn create-energy-provider [energy-handler]
  (let [energy-storage (->ForgeEnergyStorage energy-handler)]
    (reify ICapabilityProvider
      (getCapability [_ capability side]
        (when (= capability @ENERGY_CAPABILITY)
          energy-storage))
      
      (invalidate [_]
        nil))))

;; Initialize energy capability
(defn init-energy-capability! []
  (CapabilityManager/INSTANCE.register
    IEnergyStorage
    (proxy [Capability$IStorage] []
      (writeNBT [capability instance nbt]
        (.putInt nbt "energy" (.getEnergyStored instance)))
      
      (readNBT [capability instance nbt]
        (let [energy (.getInt nbt "energy")]
          (when (pos? energy)
            (.receiveEnergy instance energy false)))))
    
    #(proxy [IEnergyStorage] []
       (receiveEnergy [maxReceive simulate] 0)
       (extractEnergy [maxExtract simulate] 0)
       (getEnergyStored [] 0)
       (getMaxEnergyStored [] 0)
       (canExtract [] false)
       (canReceive [] false))))