(ns forge-impl.adapters.block-adapter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.block Block AbstractBlock$Properties]
           [net.minecraft.block.material Material]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.util ActionResultType]
           [net.minecraft.block.material.MaterialColor]
           [net.minecraft.util Direction]
           [net.minecraft.world World]
           [net.minecraft.entity.player PlayerEntity]))

(def ^:private material-map
  {:stone Material/ROCK
   :wood Material/WOOD
   :metal Material/IRON
   :glass Material/GLASS
   :air Material/AIR})

(defn- create-block-properties [mcmod-block]
  (let [material (get material-map (get-material mcmod-block) Material/ROCK)]
    (doto (AbstractBlock$Properties/create material)
      (.hardnessAndResistance (float (get-hardness mcmod-block))
                             (float (get-resistance mcmod-block)))
      (.setLightLevel (fn [_] (float (get-light-level mcmod-block))))
      (.notSolid)))) ; Allow transparency handling

(defn create-forge-block [mcmod-block]
  (proxy [Block] [(create-block-properties mcmod-block)]
    (use [state world pos player hand face hit]
      (let [result (on-activated mcmod-block
                              {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}
                              {:world world
                               :player player
                               :hand hand
                               :hit-face face
                               :hit-vec hit})]
        (if result
          ActionResultType/SUCCESS
          ActionResultType/PASS)))
    
    (onBlockPlacedBy [world pos state player stack]
      (on-placed mcmod-block
                {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}
                {:world world
                 :player player
                 :stack stack}))
    
    (onReplaced [state world pos newState isMoving]
      (on-removed mcmod-block
                {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}))
    
    (isOpaqueCube [state]
      (is-opaque? mcmod-block))
    
    (getRenderType [state]
      (case (get-render-type mcmod-block)
        :solid net.minecraft.block.BlockRenderType/MODEL
        :cutout net.minecraft.block.BlockRenderType/CUTOUT
        :cutout-mipped net.minecraft.block.BlockRenderType/CUTOUT_MIPPED
        :translucent net.minecraft.block.BlockRenderType/TRANSLUCENT
        :invisible net.minecraft.block.BlockRenderType/INVISIBLE
        net.minecraft.block.BlockRenderType/MODEL))))