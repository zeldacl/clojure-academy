(ns forge-impl.block-adapter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.block Block AbstractBlock$Properties]
           [net.minecraft.block.material Material]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.world World]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.util Direction Hand]
           [net.minecraft.util ActionResultType]))

(defn- create-block-properties [mcmod-block]
  (let [props (get-properties mcmod-block)]
    (-> (AbstractBlock$Properties/create (get-material mcmod-block))
        (.hardnessAndResistance (float (get-hardness mcmod-block))
                               (float (get-resistance mcmod-block)))
        (.setLightLevel (fn [_] (float (get-light-level mcmod-block)))))))

(defn create-forge-block [mcmod-block]
  (proxy [Block] [(create-block-properties mcmod-block)]
    (use [state world pos player hand face hit-x hit-y hit-z]
      (let [result (on-activated mcmod-block
                               {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}
                               {:world world
                                :player player
                                :hand hand
                                :face face
                                :hit [hit-x hit-y hit-z]})]
        (if result
          ActionResultType/SUCCESS
          ActionResultType/PASS)))

    (onBlockPlaced [state world pos placement ctx]
      (on-placed mcmod-block
                {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}
                {:world world
                 :state state
                 :ctx ctx}))
    
    (onBlockHarvested [world pos state player]
      (on-removed mcmod-block
                 {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}
                 {:world world
                  :state state
                  :player player}))))