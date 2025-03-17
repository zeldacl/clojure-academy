(ns cn.academy.block.matrix-position
  (:require [cn.academy.block.matrix-util :as util])
  (:import [net.minecraft.util.math BlockPos]
           [net.minecraft.util EnumFacing]))

(defprotocol IBlockPosAdapter
  (to-block-pos [this pos])
  (from-block-pos [this pos])
  (to-facing [this facing])
  (from-facing [this dir]))

(defrecord BlockPosAdapter []
  IBlockPosAdapter
  (to-block-pos [_ pos]
    (BlockPos. (:x pos) (:y pos) (:z pos)))
  
  (from-block-pos [_ ^BlockPos pos]
    {:x (.getX pos)
     :y (.getY pos)
     :z (.getZ pos)})
  
  (to-facing [_ facing]
    (case facing
      :north EnumFacing/NORTH
      :south EnumFacing/SOUTH
      :west EnumFacing/WEST
      :east EnumFacing/EAST
      :up EnumFacing/UP
      :down EnumFacing/DOWN))
  
  (from-facing [_ ^EnumFacing facing]
    (case (.getName facing)
      "north" :north
      "south" :south
      "west" :west
      "east" :east
      "up" :up
      "down" :down)))

(defn create-adapter []
  (->BlockPosAdapter))