(ns cn.academy.forge.v1_12_2.block
  (:require [cn.academy.api.block :as api])
  (:import [net.minecraft.block.properties PropertyBool PropertyInteger]
           [net.minecraft.block.material Material]
           [net.minecraft.block Block BlockContainer]
           [net.minecraft.block.state IBlockState BlockStateContainer]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.item ItemStack]
           [net.minecraft.world World]
           [net.minecraft.entity.player EntityPlayer]))

(defrecord ForgeBlockProperties []
  api/BlockProperties
  (create-boolean-property [_ name]
    (PropertyBool/create name))
  
  (create-integer-property [_ name min max]
    (PropertyInteger/create name min max))
  
  (get-block-material [_ name]
    (case name
      "rock" Material/ROCK
      (throw (IllegalArgumentException. (str "Unknown material: " name))))))

(defrecord ForgeBlockState [^IBlockState state]
  api/BlockState
  (with-property [_ property value]
    (ForgeBlockState. (.withProperty state property value)))
  
  (get-property [_ property]
    (.getValue state property))
  
  (get-default-state [_]
    (ForgeBlockState. (.getDefaultState (.getBlock state)))))

(defrecord ForgeBlockContainer [^BlockContainer container]
  api/BlockContainer
  (set-hardness! [_ hardness]
    (. container setHardness hardness))
  
  (set-harvest-level! [_ tool-class level]
    (. container setHarvestLevel tool-class level))
  
  (create-tile-entity [_ world meta]
    (. container createTileEntity world meta))
  
  (on-block-placed [_ world pos state player stack]
    (. container onBlockPlacedBy world pos state player stack))
  
  (get-actual-state [_ world pos]
    (ForgeBlockState. 
      (. container getActualState 
        (.getDefaultState container) world pos))))

(defrecord ForgeBlockFactory []
  api/ForgeBlockFactory
  (create-block-properties [_]
    (->ForgeBlockProperties))
  
  (create-block-container [_ material]
    (ForgeBlockContainer. 
      (proxy [BlockContainer] [material]
        (createTileEntity [world meta] nil))))
  
  (create-block-pos [_ x y z]
    (BlockPos. x y z))
  
  (create-item-stack [_ block count meta]
    (ItemStack. ^Block block count meta)))

;; Factory instance for 1.12.2
(def forge-factory (->ForgeBlockFactory))