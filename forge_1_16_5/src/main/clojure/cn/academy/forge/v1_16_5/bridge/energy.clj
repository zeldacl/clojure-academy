(ns cn.academy.forge.v1_16_5.bridge.energy
  (:require [cn.academy.block.matrix-energy :as energy]
            [cn.academy.energy.energy-adapter :as energy-adapter])
  (:import [net.minecraftforge.energy IEnergyStorage]
           [net.minecraftforge.common.capabilities Capability CapabilityInject]
           [net.minecraft.util Direction]))

;; --- Energy Bridge ---

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

;; --- Energy Adapter ---

(defrecord ForgeEnergyAdapter [adapter]
  IEnergyStorage
  (receiveEnergy [_ maxReceive simulate]
    (energy-adapter/receive-energy adapter maxReceive simulate))
  
  (extractEnergy [_ maxExtract simulate]
    (energy-adapter/extract-energy adapter maxExtract simulate))
  
  (getEnergyStored [_]
    (energy-adapter/get-stored-energy adapter))
  
  (getMaxEnergyStored [_]
    (energy-adapter/get-max-energy adapter))
  
  (canExtract [_]
    (energy-adapter/can-extract adapter))
  
  (canReceive [_]
    (energy-adapter/can-receive adapter)))

;; --- Public API ---

(defn create-energy-bridge []
  (->ForgeEnergyBridge))

(defn create-adapter [energy-handler]
  (->ForgeEnergyAdapter (energy-adapter/create-adapter energy-handler)))

(defn create-energy-impl []
  {:bridge (create-energy-bridge)
   :adapter-factory #(create-adapter %)})

(defn register-capability [registry]
  (let [energy-cap (CapabilityInject IEnergyStorage)]
    (.register registry energy-cap IEnergyStorage)))