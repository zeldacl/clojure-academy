(ns cljacademy.forge.tileentity.matrix-te
  (:require [cljacademy.blocks.matrix-tile :as matrix])
  (:import [net.minecraft.tileentity TileEntity]
           [net.minecraft.nbt CompoundNBT]))

(defn create-matrix-te []
  (proxy [TileEntity] []
    (write [tag]
      (let [this (matrix/create-matrix-tile)]
        (.write this tag)
        tag))
    
    (read [state tag]
      (let [this (matrix/create-matrix-tile)]
        (.read this tag)
        this))))