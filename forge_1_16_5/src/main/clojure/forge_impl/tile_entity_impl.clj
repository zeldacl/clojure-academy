(ns forge-impl.tile-entity-impl
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.tileentity TileEntity]
           [net.minecraft.nbt CompoundNBT]))

(defrecord ForgeTileEntity [^TileEntity tile]
  ITileEntity
  (tick [this]
    (.tick tile))
  
  (save [this]
    (let [tag (CompoundNBT.)]
      (.save tile tag)
      tag))
  
  (load [this data]
    (.load tile (.getDefaultState (.getBlockState tile)) data))

  (get-update-packet [this]
    (.getUpdatePacket tile))
  
  (handle-update-packet [this packet]
    (.onDataPacket tile (.getNetwork tile) packet)))