(ns forge-impl.block
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.block Block]
           [net.minecraft.block.material Material]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.world World]))

(defrecord ForgeBlock [^Block block]
  IBlock
  (get-properties [this]
    (.getProperties block))
  
  (get-material [this]
    (.getMaterial (.getProperties block)))
  
  (get-hardness [this]
    (.getHardness (.getProperties block) nil nil))
  
  (get-resistance [this]
    (.getResistance block))
  
  (get-light-level [this]
    (.getLightValue (.getProperties block)))
  
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
    (.onReplaced block
                 (.getBlockState (:world pos) (BlockPos. (:x pos) (:y pos) (:z pos)))
                 (:world pos)
                 (BlockPos. (:x pos) (:y pos) (:z pos)))))