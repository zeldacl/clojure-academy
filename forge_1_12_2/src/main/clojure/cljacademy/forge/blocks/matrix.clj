(ns cljacademy.forge.blocks.matrix
  (:require [cljacademy.blocks.matrix :as core]
            [cljacademy.forge.registry :as registry])
  (:import [net.minecraft.block Block]
           [net.minecraft.block.material Material]))

(defn register-matrix []
  (let [block (proxy [Block] [(Material/ROCK)]
                (onBlockActivated [world pos state player hand side x y z]
                  (core/handle-block-activated world pos state player))
                (onBlockPlacedBy [world pos state placer stack]
                  (core/on-block-placed world pos state placer stack)))]
    (registry/register-block block "wireless.matrix")))