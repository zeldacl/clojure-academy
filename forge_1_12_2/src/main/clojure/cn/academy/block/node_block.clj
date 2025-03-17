(ns cn.academy.block.node-block
  (:require [cn.academy.block.node :as node]
            [cn.academy.block.node-types :as types])
  (:import [net.minecraft.block Block BlockStateContainer]
           [net.minecraft.block.properties PropertyBool PropertyInteger]
           [net.minecraft.block.state IBlockState]
           [net.minecraft.util EnumFacing EnumHand]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.world World IBlockAccess]
           [net.minecraft.entity.player EntityPlayer]
           [net.minecraft.tileentity TileEntity]))

(def connected-prop (PropertyBool/create "connected"))
(def energy-prop (PropertyInteger/create "energy" 0 4))

(defrecord NodeTileEntity [node-state]
  TileEntity
  (writeToNBT [this compound]
    (proxy-super writeToNBT compound)
    ; Add NBT saving logic here
    compound)
  
  (readFromNBT [this compound]
    (proxy-super readFromNBT compound)
    ; Add NBT loading logic here
    this))

(defrecord NodeBlock [properties node-type]
  Block
  (createBlockState [this]
    (BlockStateContainer. this (into-array [connected-prop energy-prop])))
  
  (onBlockActivated [this world pos state player hand side x y z]
    ; Handle block interaction
    true)
  
  (getStateForPlacement [this world pos facing hit-x hit-y hit-z meta placer stack]
    (.getDefaultState this))
  
  (createTileEntity [this world state]
    (NodeTileEntity. (node/create-node-state node-type)))
  
  (getActualState [this state world pos]
    (let [te (.getTileEntity world pos)
          connected (if (instance? NodeTileEntity te)
                     (-> te :node-state :enabled)
                     false)
          energy (if (instance? NodeTileEntity te)
                  (let [node-state (:node-state te)
                        pct (Math/round (* 4 (/ (node/get-energy node-state)
                                               (node/get-max-energy node-state))))]
                    (min 4 pct))
                  0)]
      (-> state
          (.withProperty connected-prop connected)
          (.withProperty energy-prop energy)))))