(ns cn.academy.block.node-block-1-12
  (:require [clojure.tools.logging :as log]
            [cn.academy.block.block.block-node :as core]
            [cn.academy.protocols.block :as block-api])
  (:import [net.minecraft.block.properties PropertyBool PropertyInteger]
           [net.minecraft.block.state BlockStateContainer IBlockState]
           [net.minecraft.util EnumFacing EnumHand]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.world World IBlockAccess]
           [net.minecraft.entity.player EntityPlayer]))

;; Block state properties
(def connected-prop (PropertyBool/create "connected"))
(def energy-prop (PropertyInteger/create "energy" 0 4))

(defrecord NodeBlock1_12 [core-block]
  block-api/IForgeBlock
  (createBlockState [_]
    (BlockStateContainer. this (into-array [connected-prop energy-prop])))
  
  (getActualState [_ state world pos]
    (if-let [tile (.getTileEntity world pos)]
      (let [node-state (.getNodeState tile)
            energy (get-in node-state [:energy])
            max-energy (get-in node-state [:properties :max-energy])
            pct (Math/min 4 (Math/round (* 4 (/ energy max-energy))))]
        (-> state
            (.withProperty connected-prop (boolean (:active node-state)))
            (.withProperty energy-prop (int pct))))
      state))
  
  (onBlockActivated [_ world pos state player hand facing hitX hitY hitZ]
    (when (not (.isSneaking player))
      (core/get-container core-block player world pos)
      true))

  (getStateFromMeta [_ meta]
    (.getDefaultState this))

  (getMetaFromState [_ state]
    0)
  
  (createNewTileEntity [_ world meta]
    (core/create-tile-entity core-block world meta)))

(defn create-node-block [type]
  (->NodeBlock1_12 (core/create-node-block type)))