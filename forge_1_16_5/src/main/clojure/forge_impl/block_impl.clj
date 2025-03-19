(ns forge-impl.block-impl
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.block Block AbstractBlock$Properties]
           [net.minecraft.block.material Material]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.world World]))

(defrecord ForgeBlock [^Block block]
  IBlock
  (get-properties [this]
    (.getProperties block))
  
  (get-material [this]
    (.getMaterial (.getDefaultState block)))
  
  (get-hardness [this]
    (.getBlockHardness (.getDefaultState block) nil nil))
  
  (get-resistance [this]
    (.getExplosionResistance block))
  
  (get-light-level [this]
    (.getLightValue (.getDefaultState block)))
  
  (on-activated [this pos data]
    (.onBlockActivated block
                     (:world data)
                     (BlockPos. (:x pos) (:y pos) (:z pos))
                     (:player data)
                     (:hand data)
                     (:hit data)))
  
  (on-placed [this pos data]
    (.onBlockPlaced block
                   (:world data)
                   (BlockPos. (:x pos) (:y pos) (:z pos))
                   (:state data)))
  
  (on-removed [this pos]
    (.onBlockHarvested block
                      (:world pos)
                      (BlockPos. (:x pos) (:y pos) (:z pos))
                      (.getBlockState (:world pos) (BlockPos. (:x pos) (:y pos) (:z pos)))
                      (:player pos))))