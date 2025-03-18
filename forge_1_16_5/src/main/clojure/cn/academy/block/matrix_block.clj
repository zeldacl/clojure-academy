(ns cn.academy.block.matrix-block
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-energy :as energy]
            [cn.academy.energy.energy-adapter :as energy-adapter]
            [cn.academy.block.matrix-structure :as structure])
  (:import [net.minecraft.block Block BlockState]
           [net.minecraft.tileentity TileEntity]
           [net.minecraft.util Direction]
           [net.minecraftforge.common.capabilities Capability CapabilityInject ICapabilityProvider]
           [net.minecraftforge.energy CapabilityEnergy IEnergyStorage]))

(def ^:dynamic *ENERGY_CAPABILITY* nil)

(defrecord MatrixBlock []
  Block
  (createTileEntity [_ state world]
    (->MatrixTileEntity))
  
  (onBlockPlacedBy [_ world pos state placer stack]
    (when-let [tile (.getTileEntity world pos)]
      (.setPlayerId tile (.getUUID placer))))
  
  (onReplaced [_ oldState newState world pos block-flags]
    (when-not (= (.getBlock oldState) (.getBlock newState))
      (structure/break-structure! world pos)
      (.removeTileEntity world pos))))

(defrecord MatrixTileEntity []
  TileEntity
  (getCapability [this cap dir]
    (if (= cap *ENERGY_CAPABILITY*)
      (energy-adapter/create-adapter (energy/create-energy-handler (.getMatrix this)))
      (.getCapability (super this) cap dir)))
  
  (read [this state nbt]
    (.read (super this) state nbt)
    (matrix/load-from-nbt! (.getMatrix this) nbt))
  
  (write [this nbt]
    (.write (super this) nbt)
    (merge nbt (matrix/save-to-nbt (.getMatrix this)))))

(defn register-block []
  (let [block (->MatrixBlock)
        tile-type (->MatrixTileEntity)]
    (Registry/register (Registry/BLOCK) 
                      (ResourceLocation. "academy" "matrix") 
                      block)
    (Registry/register (Registry/TILE_ENTITY_TYPE)
                      (ResourceLocation. "academy" "matrix")
                      tile-type)))