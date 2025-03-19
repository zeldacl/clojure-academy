(ns forge-impl.capability-impl
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraftforge.common.capabilities Capability ICapabilityProvider]
           [net.minecraftforge.common.util LazyOptional]
           [net.minecraft.util Direction]
           [net.minecraftforge.energy IEnergyStorage]))

(defrecord ForgeCapabilityProvider [provider caps-atom]
  ICapabilityProvider
  (has-capability? [_ cap side]
    (when-let [forge-cap (get @caps-atom cap)]
      (.isPresent (.getCapability provider forge-cap (or side nil)))))
  
  (get-capability [_ cap side]
    (when-let [forge-cap (get @caps-atom cap)]
      (.getCapability provider forge-cap (or side nil))))
  
  (invalidate-capabilities [_]
    (doseq [[_ cap] @caps-atom]
      (.invalidate cap)))

  (get-capability-impl [_ capability side]
    (when-let [cap (get @caps-atom capability)]
      (.getCapability provider cap (or side nil))))

  (has-capability [_ capability side]
    (when-let [cap (get @caps-atom capability)] 
      (.isPresent (.getCapability provider cap (or side nil))))))

;; Energy capability implementation
(defrecord ForgeEnergyStorage [energy max-energy]
  IEnergyStorage
  (receiveEnergy [this maxReceive simulate]
    (let [space (- max-energy @energy)
          amount (min maxReceive space)]
      (when-not simulate
        (swap! energy + amount))
      amount))
  
  (extractEnergy [this maxExtract simulate]
    (let [amount (min maxExtract @energy)]
      (when-not simulate
        (swap! energy - amount))
      amount))
  
  (getEnergyStored [_]
    @energy)
  
  (getMaxEnergyStored [_]
    max-energy)
  
  (canExtract [_] true)
  
  (canReceive [_] true))

(defn create-capability-provider []
  (->ForgeCapabilityProvider {} (atom {})))

(defn create-energy-storage [max-energy]
  (->ForgeEnergyStorage (atom 0) max-energy))