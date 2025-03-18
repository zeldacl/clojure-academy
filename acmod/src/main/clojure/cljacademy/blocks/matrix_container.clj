(ns cljacademy.blocks.matrix-container
  (:require [cljacademy.api.container :as container]
            [cljacademy.blocks.matrix-tile :as tile])
  (:import [net.minecraft.entity.player EntityPlayer]))

(defrecord MatrixContainer [tile player]
  container/IContainer
  (can-interact-with [this player]
    (let [tile-pos (.getPos tile)]
      (.withinDistance player 
                      (.getX tile-pos) 
                      (.getY tile-pos) 
                      (.getZ tile-pos) 
                      64.0)))
  
  (get-energy-info [this]
    {:current (.getCurrentEnergy tile)
     :max (.getMaxEnergy tile)})
  
  (transfer-energy [this amount]
    (.receiveEnergy tile amount false)))

(defn create-container [tile player]
  (->MatrixContainer tile player))