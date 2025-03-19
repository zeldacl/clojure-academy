(ns forge-impl.block-converter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.block Block AbstractBlock$Properties]
           [net.minecraft.block.material Material]
           [net.minecraft.util.math BlockPos]
           [net.minecraftforge.registries ForgeRegistries]))

(defn create-block-properties [block]
  (let [properties (get-properties block)]
    (-> (AbstractBlock$Properties/create (get-material block))
        (.hardnessAndResistance (float (get-hardness block)) 
                               (float (get-resistance block)))
        (.setLightLevel (fn [_] (float (get-light-level block)))))))

(defn convert-to-forge-block [mcmod-block]
  (proxy [Block] [(create-block-properties mcmod-block)]
    (onBlockActivated [state world pos player hand hit]
      (on-activated mcmod-block 
                   {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}
                   {:world world :player player :hand hand :hit hit}))
    
    (onBlockPlaced [state world pos placement ctx]
      (on-placed mcmod-block
                {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}
                {:world world :state state}))
    
    (onReplaced [state world pos newState isMoving]
      (on-removed mcmod-block
                 {:x (.getX pos) :y (.getY pos) :z (.getZ pos)
                  :world world}))))