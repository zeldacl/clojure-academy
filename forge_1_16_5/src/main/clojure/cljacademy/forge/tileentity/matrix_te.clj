(ns cljacademy.forge.tileentity.matrix-te
  (:require [cljacademy.blocks.matrix-tile :as matrix])
  (:import [net.minecraft.tileentity TileEntity]
           [net.minecraft.nbt CompoundNBT]))

(defn create-matrix-te []
  (proxy [TileEntity] []
    (save [tag]
      (let [this (matrix/create-matrix-tile)]
        (.save this tag)
        tag))
    
    (load [state tag]
      (let [this (matrix/create-matrix-tile)]
        (.load this tag)
        this))))