(ns cn.academy.forge-1-16.energy
  (:require [cn.academy.api.energy :as energy-api])
  (:import [net.minecraftforge.energy IEnergyStorage]
           [net.minecraft.tileentity TileEntity]
           [net.minecraftforge.common.capabilities Capability CapabilityInject]
           [net.minecraft.util Direction]))

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
    (getCapability [_ capability direction]
      (if (= capability net.minecraftforge.energy.CapabilityEnergy/ENERGY)
        (net.minecraftforge.common.util.LazyOptional/of #(identity capability-instance))
        net.minecraftforge.common.util.LazyOptional/EMPTY))))

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
        chunk-pos-range (for [x (range (bit-shift-right (.getX min-pos) 4) 
                                     (inc (bit-shift-right (.getX max-pos) 4)))
                             z (range (bit-shift-right (.getZ min-pos) 4)
                                    (inc (bit-shift-right (.getZ max-pos) 4)))]
                         (net.minecraft.util.math.ChunkPos. x z))
        tile-entities (mapcat #(.getTileEntities (.getChunkFromPos world %)) chunk-pos-range)]
    (->> tile-entities
         (filter #(.isPresent (.getCapability % net.minecraftforge.energy.CapabilityEnergy/ENERGY nil)))
         (into []))))

(defn create-energy-impl []
  {:get-nearby-nodes get-nearby-nodes})