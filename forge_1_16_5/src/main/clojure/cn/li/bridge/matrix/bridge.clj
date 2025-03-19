(ns cn.li.bridge.matrix.bridge
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-energy :as energy]
            [cn.academy.block.matrix-inventory :as inventory]
            [cn.academy.block.matrix-network :as network]
            [cn.academy.block.matrix-state :as state])
  (:import [net.minecraft.block Block AbstractBlock$Properties]
           [net.minecraft.block.material Material]
           [net.minecraft.tileentity TileEntity]
           [net.minecraft.nbt CompoundNBT]
           [net.minecraftforge.common.capabilities Capability ICapabilityProvider]
           [net.minecraftforge.energy CapabilityEnergy IEnergyStorage]
           [net.minecraftforge.common.util LazyOptional]
           [net.minecraft.util Direction]))

(defprotocol IMatrixBridge
  "Bridge between matrix implementation and Forge"
  (create-matrix-block [this matrix]
    "Create a Forge block for the matrix")
  (create-matrix-tile [this matrix]
    "Create a Forge tile entity for the matrix")
  (register-matrix [this registry id matrix]
    "Register the matrix with Forge registry"))

(deftype ForgeMatrixTile [matrix capabilities]
  TileEntity
  (load [_ nbt]
    (state/handle-sync! matrix (nbt/from-nbt nbt)))
  
  (save [_ nbt]
    (let [state {:formed? (state/is-formed? matrix)
                 :energy (state/get-energy-stored matrix)}]
      (nbt/to-nbt nbt state)))
  
  (getCapability [_ cap side]
    (let [side-dir (when side (.get side))]
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

  ICapabilityProvider
  (invalidateCaps [_]
    (doseq [[_ cap] @capabilities]
      (.invalidate cap))
    (reset! capabilities {})))

(deftype ForgeMatrixBlock [matrix]
  Block
  (use [_ state world pos player hand hit]
    (matrix/on-activated matrix pos {:player player :hand hand}))
  
  (onBlockPlacedBy [_ world pos state placer stack]
    (matrix/on-placed matrix pos {:placer placer :stack stack}))
  
  (onReplaced [_ old-state new-state world pos is-moving]
    (matrix/on-removed matrix pos)))

(defrecord ForgeMatrixBridge []
  IMatrixBridge
  (create-matrix-block [_ matrix]
    (ForgeMatrixBlock. matrix))
  
  (create-matrix-tile [_ matrix]
    (ForgeMatrixTile. matrix (atom {})))
  
  (register-matrix [_ registry id matrix]
    (let [block (create-matrix-block matrix)
          tile (create-matrix-tile matrix)]
      (.register registry id block)
      (.register registry (str id "_tile") tile))))

(defn create-bridge []
  (->ForgeMatrixBridge))