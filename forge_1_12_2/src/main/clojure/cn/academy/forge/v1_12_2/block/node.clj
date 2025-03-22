(ns cn.academy.forge.v1-12-2.block.node
  (:require [cn.academy.protocols.block :as block-api])
  (:import [net.minecraft.block Block]
           [net.minecraft.block.material Material]
           [net.minecraft.block.state IBlockState]
           [net.minecraft.block.properties PropertyBool PropertyInteger PropertyEnum]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.world World]
           [net.minecraft.entity.player EntityPlayer]))

(defrecord NodeBlockProperties []
  block-api/BlockProperties
  (create-boolean-property [_ name]
    (PropertyBool/create name))
  
  (create-integer-property [_ name min max]
    (PropertyInteger/create name min max))
  
  (create-enum-property [_ name values]
    (PropertyEnum/create name NodeType)))

;; Block container implementation for 1.12.2
(defrecord NodeBlockContainer [^Block block]
  block-api/BlockContainer
  (set-hardness! [_ hardness]
    (.setHardness block hardness))
  
  (set-harvest-level! [_ tool level]
    (.setHarvestLevel block tool level))
  
  (add-property! [_ property]
    (.addProperty block property))
  
  (set-block-state-handler! [_ handler]
    (.setBlockStateHandler block handler))
  
  (set-tile-entity-provider! [_ class-name]
    (.setTileEntityClass block class-name))
  
  (set-node-properties! [_ properties]
    (.setNodeProperties block properties)))

;; Factory implementation for 1.12.2
(defrecord NodeBlockFactory []
  block-api/ForgeBlockFactory
  (create-block-properties [_]
    (->NodeBlockProperties))
  
  (create-block-container [_ material]
    (->NodeBlockContainer 
      (proxy [Block] [material]
        (createTileEntity [world meta]
          nil))))
  
  (get-block-material [_ type]
    (case type
      "rock" Material/ROCK
      (throw (IllegalArgumentException. (str "Unknown material: " type))))))

;; Export factory instance
(def forge-factory (->NodeBlockFactory))