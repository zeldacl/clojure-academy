(ns cn.li.bridge.matrix.capability
  (:require [cn.academy.block.matrix-energy :as energy]
            [cn.academy.block.matrix-inventory :as inventory]
            [cn.academy.block.matrix-state :as state])
  (:import [net.minecraftforge.common.capabilities Capability ICapabilityProvider]
           [net.minecraftforge.energy CapabilityEnergy IEnergyStorage]
           [net.minecraftforge.items IItemHandler]
           [net.minecraft.util Direction]
           [net.minecraft.nbt CompoundNBT]))

(defrecord MatrixCapabilityProvider [matrix capabilities]
  ICapabilityProvider
  (getCapability [_ cap side]
    (cond
      (= cap CapabilityEnergy/ENERGY)
      (LazyOptional/of #(reify IEnergyStorage
                         (receiveEnergy [_ amount simulate]
                           (energy/receive-energy matrix amount simulate))
                         (extractEnergy [_ amount simulate]
                           (energy/extract-energy matrix amount simulate))
                         (getEnergyStored [_]
                           (energy/get-energy-stored matrix))
                         (getMaxEnergyStored [_]
                           (energy/get-energy-capacity matrix))
                         (canExtract [_]
                           (energy/can-extract? matrix))
                         (canReceive [_]
                           (energy/can-receive? matrix))))

      :else LazyOptional/EMPTY)))

(defn create-capability-provider [matrix]
  (->MatrixCapabilityProvider matrix (atom {})))