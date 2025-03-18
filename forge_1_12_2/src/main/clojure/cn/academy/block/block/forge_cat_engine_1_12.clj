(ns cn.academy.block.block.forge-cat-engine-1-12
  (:require [cn.academy.block.block.block-cat-engine :as core]
            [cn.academy.forge-1-12.block :as forge-block])
  (:import [net.minecraft.block.state IBlockState]
           [net.minecraft.util EnumBlockRenderType]))

(defrecord ForgeCatEngine112 [core-block]
  forge-block/IForgeBlock
  (create-tile-entity [_ world meta]
    (forge-block/create-tile-entity-bridge 
      (core/create-tile-entity core-block world meta)))
  
  (on-block-activated [_ world pos state player hand facing hit-x hit-y hit-z]
    (core/on-block-activated core-block world pos state player hand facing hit-x hit-y hit-z))
  
  (is-opaque-cube [_ ^IBlockState state]
    (core/is-opaque-cube? core-block state))
  
  (get-render-type [_ ^IBlockState state]
    (condp = (core/get-render-type core-block state)
      :invisible EnumBlockRenderType/INVISIBLE
      EnumBlockRenderType/MODEL)))

(defn create []
  (->ForgeCatEngine112 (core/create)))