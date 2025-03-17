(ns cn.academy.block.matrix-position
  (:require [cn.academy.block.matrix-util :as util])
  (:import [net.minecraft.util.math BlockPos Direction Vector3i]
           [net.minecraft.util Direction$Axis]))

(defprotocol IBlockPosAdapter
  (to-block-pos [this pos])
  (from-block-pos [this pos])
  (to-direction [this facing])
  (from-direction [this dir]))

(defrecord BlockPosAdapter []
  IBlockPosAdapter
  (to-block-pos [_ pos]
    (BlockPos. (:x pos) (:y pos) (:z pos)))
  
  (from-block-pos [_ ^BlockPos pos]
    {:x (.getX pos)
     :y (.getY pos)
     :z (.getZ pos)})
  
  (to-direction [_ facing]
    (case facing
      :north Direction/NORTH
      :south Direction/SOUTH
      :west Direction/WEST
      :east Direction/EAST
      :up Direction/UP
      :down Direction/DOWN))
  
  (from-direction [_ ^Direction dir]
    (case (.name dir)
      "NORTH" :north
      "SOUTH" :south
      "WEST" :west
      "EAST" :east
      "UP" :up
      "DOWN" :down)))

(defn create-adapter []
  (->BlockPosAdapter))