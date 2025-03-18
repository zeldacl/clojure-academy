(ns cljacademy.forge.tileentity.matrix-te
  (:require [cljacademy.blocks.matrix-tile :as matrix])
  (:import [net.minecraft.tileentity TileEntity]))

(defn create-matrix-te []
  (proxy [TileEntity] []
    (writeToNBT [tag]
      (let [this (matrix/create-matrix-tile)]
        (.writeToNBT this tag)
        tag))
    
    (readFromNBT [tag]
      (let [this (matrix/create-matrix-tile)]
        (.readFromNBT this tag)
        this))))