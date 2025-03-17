(ns cn.academy.forge.v1-15-2.block.node
  (:require [cn.academy.api.block :as block-api])
  (:import [net.minecraft.block Block]
           [net.minecraft.block.material Material]
           [net.minecraft.state BooleanProperty IntegerProperty EnumProperty]
           [net.minecraft.block.BlockState]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.world World]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.block AbstractBlock$Properties]))

(defrecord NodeBlockProperties []
  block-api/BlockProperties
  (create-boolean-property [_ name]
    (BooleanProperty/create name))
  
  (create-integer-property [_ name min max]
    (IntegerProperty/create name min max))
  
  (create-enum-property [_ name values]
    (EnumProperty/create name NodeType)))

;; Block container implementation for 1.15.2
(defrecord NodeBlockContainer [^Block block]
  block-api/BlockContainer
  (set-hardness! [_ hardness]
    (.. block properties (hardnessAndResistance hardness)))
  
  (set-harvest-level! [_ tool level]
    (.. block properties (harvestLevel level)))
  
  (add-property! [_ property]
    (.addProperty block property))
  
  (set-block-state-handler! [_ handler]
    (.setBlockStateHandler block handler))
  
  (set-tile-entity-provider! [_ class-name]
    (.setTileEntityClass block class-name))
  
  (set-node-properties! [_ properties]
    (.setNodeProperties block properties)))

;; Factory implementation for 1.15.2
(defrecord NodeBlockFactory []
  block-api/ForgeBlockFactory
  (create-block-properties [_]
    (->NodeBlockProperties))
  
  (create-block-container [_ material]
    (->NodeBlockContainer 
      (proxy [Block] [(-> (AbstractBlock$Properties/create material))]
        (createTileEntity [state world]
          nil))))
  
  (get-block-material [_ type]
    (case type
      "rock" Material/ROCK
      (throw (IllegalArgumentException. (str "Unknown material: " type))))))

;; Export factory instance
(def forge-factory (->NodeBlockFactory))