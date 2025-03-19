(ns forge-impl.tile-entity
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.tileentity TileEntity]
           [net.minecraft.network.play.server SUpdateTileEntityPacket]))

(defrecord ForgeTileEntity [^TileEntity tile-entity]
  ITileEntity
  (tick [this]
    (.tick tile-entity))
  
  (save [this]
    (.save tile-entity))
  
  (load [this data]
    (.load tile-entity (:block-state data) (:nbt data)))
  
  (get-update-packet [this]
    (.getUpdatePacket tile-entity))
  
  (handle-update-packet [this packet]
    (.onDataPacket tile-entity (:network packet) packet)))