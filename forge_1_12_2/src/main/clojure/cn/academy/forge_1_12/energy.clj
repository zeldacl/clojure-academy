(ns cn.academy.forge-1-12.energy
  (:require [cn.academy.api.energy :as energy-api])
  (:import [net.minecraftforge.energy CapabilityEnergy IEnergyStorage]
           [net.minecraftforge.common.capabilities Capability CapabilityInject]))

(defn create-forge-energy-storage [^IEnergyStorage storage]
  (reify energy-api/IEnergyCapability
    (get-stored-energy [_]
      (.getEnergyStored storage))
    
    (get-max-energy [_]
      (.getMaxEnergyStored storage))
    
    (receive-energy [_ amount simulate]
      (.receiveEnergy storage amount simulate))))

(defn create-capability-provider [capability-instance]
  (reify net.minecraftforge.common.capabilities.ICapabilityProvider
    (hasCapability [_ capability facing]
      (= capability (CapabilityEnergy/ENERGY)))
    
    (getCapability [_ capability facing]
      (when (= capability (CapabilityEnergy/ENERGY))
        capability-instance))))

(defn get-nearby-nodes [world pos]
  (let [range 16
        min-pos (net.minecraft.util.math.BlockPos. 
                  (- (.getX pos) range)
                  (- (.getY pos) range)
                  (- (.getZ pos) range))
        max-pos (net.minecraft.util.math.BlockPos. 
                  (+ (.getX pos) range)
                  (+ (.getY pos) range)
                  (+ (.getZ pos) range))
        tile-entities (.getTileEntities world min-pos max-pos)]
    (filter #(.hasCapability % (CapabilityEnergy/ENERGY) nil) tile-entities)))

(defn create-energy-impl []
  {:get-nearby-nodes get-nearby-nodes})