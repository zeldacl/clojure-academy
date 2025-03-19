(ns forge-impl.tile-entity-converter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.tileentity TileEntity TileEntityType]
           [net.minecraft.nbt CompoundNBT]
           [net.minecraft.network.play.server SUpdateTileEntityPacket]))

(defn create-tile-entity-type [mcmod-te block]
  (TileEntityType/Builder/create
    (fn []
      (proxy [TileEntity] []
        (tick []
          (tick mcmod-te))
        
        (save [tag]
          (let [mcmod-tag (save mcmod-te)]
            (.putAll tag mcmod-tag)
            tag))
        
        (load [state tag]
          (load mcmod-te {:block-state state :nbt tag}))
        
        (getUpdatePacket []
          (get-update-packet mcmod-te))
        
        (onDataPacket [net packet]
          (handle-update-packet mcmod-te 
                              {:network net 
                               :packet packet}))))
    (into-array [block])
    .build
    nil))

(defn convert-to-forge-te-type [mcmod-te block]
  (create-tile-entity-type mcmod-te block))