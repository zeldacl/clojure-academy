(ns forge-impl.tile-entity-adapter
  (:require [mcmod.protocols :refer :all]
            [forge-impl.capability-impl :as cap-impl])
  (:import [net.minecraft.tileentity TileEntity TileEntityType]
           [net.minecraft.nbt CompoundNBT]
           [net.minecraft.network NetworkManager]
           [net.minecraft.network.play.server SUpdateTileEntityPacket]
           [net.minecraft.world World]
           [net.minecraft.util Direction]))

(defrecord ForgeTileEntity [^TileEntity tile-entity mcmod-te]
  ITileEntity
  (tick [_]
    (tick mcmod-te))
  
  (save [_]
    (let [tag (CompoundNBT.)]
      (write-to-nbt mcmod-te tag)
      tag))
  
  (load [_ data]
    (read-from-nbt mcmod-te data))
  
  (get-update-packet [_]
    (let [tag (CompoundNBT.)]
      (write-to-nbt mcmod-te tag)
      (SUpdateTileEntityPacket. (.getPos tile-entity) -1 tag)))
  
  (handle-update-packet [_ packet]
    (handle-update-packet mcmod-te (.getNbtCompound packet)))
  
  (on-load [_]
    (on-load mcmod-te))
  
  (on-unload [_]
    (on-unload mcmod-te))
  
  (get-capability [_ cap side]
    (get-capability mcmod-te cap side))

  (get-position [_]
    (let [pos (.getPos tile-entity)]
      {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}))
  
  (get-block-type [_]
    (.getBlockState tile-entity))
  
  (get-block-state [_]
    (.getBlockState (.getWorld tile-entity) (.getPos tile-entity)))
  
  (read-from-nbt [_ nbt]
    (read-from-nbt mcmod-te nbt))
  
  (write-to-nbt [_ nbt]
    (write-to-nbt mcmod-te nbt))
  
  (mark-dirty [_]
    (.markDirty tile-entity))
  
  (get-capabilities [_ side]
    (get-capabilities mcmod-te side)))

(defn create-forge-tile-entity [mcmod-te block-type]
  (proxy [TileEntity] [block-type]
    (tick []
      (tick mcmod-te))
    
    (save [tag]
      (write-to-nbt mcmod-te tag))
    
    (load [tag]
      (read-from-nbt mcmod-te tag))
    
    (getUpdatePacket []
      (get-update-packet mcmod-te))
    
    (onDataPacket [net packet]
      (handle-update-packet mcmod-te packet))
    
    (getCapability [capability face]
      (get-capability mcmod-te capability face))
    
    (hasCapability [capability face]
      (has-capability mcmod-te capability face))))

(defn init! []
  ;; Any needed initialization for tile entities
  nil)