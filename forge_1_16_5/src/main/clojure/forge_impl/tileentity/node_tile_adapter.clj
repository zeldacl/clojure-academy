(ns forge-impl.tileentity.node-tile-adapter
  (:require [mcmod.protocols :refer :all]
            [forge-impl.energy.energy-adapter :as energy])
  (:import [net.minecraft.tileentity TileEntity TileEntityType]
           [net.minecraft.nbt CompoundNBT]
           [net.minecraft.util Direction]
           [net.minecraftforge.common.capabilities Capability ICapabilityProvider CapabilityInject]
           [net.minecraftforge.energy IEnergyStorage]
           [net.minecraft.network NetworkManager]
           [net.minecraft.network.play.server SUpdateTileEntityPacket]))

;; Create Forge tile entity implementation
(defn create-forge-tile-entity [mcmod-te te-type]
  (proxy [TileEntity ICapabilityProvider] [te-type]
    ;; NBT handling
    (write [nbt]
      (proxy-super write nbt)
      (write-to-nbt mcmod-te nbt)
      nbt)
    
    (read [state nbt]
      (proxy-super read state nbt)
      (read-from-nbt mcmod-te nbt))
    
    ;; Capabilities
    (getCapability [capability side]
      (if (= capability @energy/ENERGY_CAPABILITY)
        (energy/create-energy-provider mcmod-te)
        (proxy-super getCapability capability side)))
    
    ;; Network sync
    (getUpdatePacket []
      (let [nbt (CompoundNBT.)]
        (write-to-nbt mcmod-te nbt)
        (SUpdateTileEntityPacket. (.getPos this) -1 nbt)))
    
    (getUpdateTag []
      (let [nbt (CompoundNBT.)]
        (write-to-nbt mcmod-te nbt)
        nbt))
    
    (onDataPacket [net packet]
      (read-from-nbt mcmod-te (.getNbtCompound packet)))
    
    (handleUpdateTag [state nbt]
      (read-from-nbt mcmod-te nbt))
    
    ;; Block updates
    (markDirty []
      (proxy-super markDirty)
      (when-let [world (.getWorld this)]
        (.notifyBlockUpdate world 
                          (.getPos this)
                          (.getBlockState this)
                          (.getBlockState this)
                          3)))))

;; Create tile entity type
(defn create-tile-entity-type [block supplier]
  (-> (TileEntityType$Builder/create supplier (into-array [block]))
      (.build nil)))

;; Register tile entity method
(defmethod forge-impl.tile-entity-adapter/create-forge-tile-entity "node"
  [mcmod-te te-type]
  (create-forge-tile-entity mcmod-te te-type))