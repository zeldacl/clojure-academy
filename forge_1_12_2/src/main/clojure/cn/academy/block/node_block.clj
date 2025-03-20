(ns cn.academy.block.node-block
  (:require [cn.academy.block.node :as node]
            [cn.academy.block.node-types :as types]
            [cn.academy.block.tileentity.tile-node :as tile-node])
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

(defn create-tile-entity [node-type]
  (tile-node/create-node-tile node-type))

(defn get-node-info [world pos]
  (let [te (.getTileEntity world pos)]
    (let [connected (if (instance? tile-node/TileNode te)
                     (count (get-connected-nodes world pos))
                     0)
          energy (if (instance? tile-node/TileNode te)
                  (.getEnergy te)
                  0.0)]
      {:connected connected
       :energy energy})))

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
    (create-tile-entity node-type))
  
  (getActualState [this state world pos]
    (let [{:keys [connected energy]} (get-node-info world pos)]
      (-> state
          (.withProperty connected-prop connected)
          (.withProperty energy-prop energy)))))