(ns cn.academy.forge.v1-15-2.block
  (:require [cn.academy.protocols.block :as block-api])
  (:import [net.minecraft.state BooleanProperty IntegerProperty]
           [net.minecraft.block.material Material]
           [net.minecraft.block Block AbstractBlock$Properties]
           [net.minecraft.block.state BlockState]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.item ItemStack]
           [net.minecraft.world World]
           [net.minecraft.entity.player PlayerEntity]))

(defrecord ForgeBlockProperties []
  block-api/BlockProperties
  (create-boolean-property [_ name]
    (BooleanProperty/create name))
  
  (create-integer-property [_ name min max]
    (IntegerProperty/create name min max))
  
  (get-block-material [_ name]
    (case name
      "rock" Material/ROCK
      (throw (IllegalArgumentException. (str "Unknown material: " name))))))

(defrecord ForgeBlockState [^BlockState state]
  block-api/BlockState
  (with-property [_ property value]
    (ForgeBlockState. (.with state property value)))
  
  (get-property [_ property]
    (.get state property))
  
  (get-default-state [_]
    (ForgeBlockState. (.getDefaultState (.getBlock state)))))

(defrecord ForgeBlockContainer [^Block block]
  block-api/BlockContainer
  (set-hardness! [_ hardness]
    (.hardnessAndResistance (.properties block) hardness))
  
  (set-harvest-level! [_ tool-class level]
    (.harvestLevel (.properties block) level))
  
  (create-tile-entity [_ world meta]
    (.createTileEntity block (ForgeBlockState. (.getDefaultState block)) world))
  
  (on-block-placed [_ world pos state player stack]
    (.onBlockPlacedBy block world pos state player stack))
  
  (get-actual-state [_ world pos]
    (ForgeBlockState. 
      (.getDefaultState block))))

(defrecord ForgeBlockFactory []
  block-api/ForgeBlockFactory
  (create-block-properties [_]
    (->ForgeBlockProperties))
  
  (create-block-container [_ material]
    (ForgeBlockContainer.
      (proxy [Block] [(-> (AbstractBlock$Properties/create material))]
        (createTileEntity [state world] nil))))
  
  (create-block-pos [_ x y z]
    (BlockPos. x y z))
  
  (create-item-stack [_ block count meta]
    (ItemStack. ^Block block count)))  ; Note: meta is ignored in 1.15+

;; Factory instance for 1.15.2
(def forge-factory (->ForgeBlockFactory))