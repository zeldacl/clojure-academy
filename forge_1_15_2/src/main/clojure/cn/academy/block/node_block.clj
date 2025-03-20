(ns cn.academy.block.node-block
  (:require [cn.academy.block.node :as node]
            [cn.academy.block.node-types :as types]
            [cn.academy.block.tileentity.tile-node :as tile-node])
  (:import [net.minecraft.block Block AbstractBlock$Properties BlockState]
           [net.minecraft.state StateContainer$Builder]
           [net.minecraft.state.properties BooleanProperty IntegerProperty]
           [net.minecraft.util Direction]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.world World IBlockReader]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.tileentity TileEntity]))

(def connected-prop (BooleanProperty/create "connected"))
(def energy-prop (IntegerProperty/create "energy" 0 4))

(defrecord NodeBlock [block-properties node-type]
  Block
  (fillStateContainer [this builder]
    (.add builder (into-array [connected-prop energy-prop]))
    nil)
  
  (onBlockActivated [this state world pos player hand hit-result]
    ; Handle block interaction
    true)
  
  (getStateForPlacement [this context]
    (.getDefaultState this))
  
  (createTileEntity [this state]
    (tile-node/create-node-tile node-type)))

(defn create-node-block [node-type properties]
  (->NodeBlock properties node-type))

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