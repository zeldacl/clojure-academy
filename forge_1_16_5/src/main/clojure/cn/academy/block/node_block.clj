(ns cn.academy.block.node-block
  (:require [cn.academy.block.node :as node]
            [cn.academy.block.node-types :as types])
  (:import [net.minecraft.block Block AbstractBlock$Properties BlockState]
           [net.minecraft.state StateContainer$Builder]
           [net.minecraft.state.properties BooleanProperty IntegerProperty]
           [net.minecraft.util Direction IStringSerializable]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.world World IBlockReader]
           [net.minecraft.world.server ServerWorld]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.tileentity TileEntity]))

(def connected-prop (BooleanProperty/create "connected"))
(def energy-prop (IntegerProperty/create "energy" 0 4))

(defrecord NodeTileEntity [node-state]
  TileEntity
  (save [this compound]
    (proxy-super save compound)
    ; Add NBT saving logic here
    compound)
  
  (load [this state compound]
    (proxy-super load state compound)
    ; Add NBT loading logic here
    this))

(defrecord NodeBlock [block-properties node-type]
  Block
  (createBlockStateDefinition [this builder]
    (.add builder (into-array [connected-prop energy-prop]))
    nil)
  
  (use [this state world pos player hand hit-result]
    ; Handle block interaction
    true)
  
  (getStateForPlacement [this context]
    (.defaultBlockState this))
  
  (createTileEntity [this state world]
    (NodeTileEntity. (node/create-node-state node-type))))

(defn create-node-block [node-type properties]
  (->NodeBlock properties node-type))