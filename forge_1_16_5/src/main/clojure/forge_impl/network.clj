(ns forge-impl.network
  (:require [mcmod.protocols :as proto])
  (:import [net.minecraft.network PacketBuffer]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.world World]
           [net.minecraft.tileentity TileEntity]))

;; PacketBuffer implementation
(extend-type PacketBuffer
  proto/IBuffer
  (write-long [this value]
    (.writeLong this value))
  (read-long [this]
    (.readLong this))
  (write-double [this value]
    (.writeDouble this value))
  (read-double [this]
    (.readDouble this))
  (write-boolean [this value]
    (.writeBoolean this value))
  (read-boolean [this]
    (.readBoolean this))
  (write-string [this value]
    (.writeString this value))
  (read-string [this]
    (.readString this)))

;; BlockPos implementation  
(extend-type BlockPos
  proto/IBlockPos
  (pos->long [this]
    (.toLong this))
  (long->pos [_]
    (BlockPos/fromLong value)))

;; World implementation
(extend-type World
  proto/IWorld
  (get-tile-entity [this pos]
    (.getTileEntity this pos)))

;; TileEntity implementation for nodes
(extend-type TileEntity
  proto/INode
  (node? [this]
    (instance? net.minecraft.tileentity.TileEntity this))
  (set-node-energy! [this energy]
    (.setEnergy this energy))
  (set-node-enabled! [this enabled]
    (.setEnabled this enabled))
  (set-node-config! [this name password]
    (.setConfig this name password)))