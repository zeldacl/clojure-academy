(ns cljacademy.forge.blocks.matrix
  (:require [cljacademy.blocks.matrix :as core]
            [cljacademy.forge.registry :as registry])
  (:import [net.minecraft.block Block]
           [net.minecraft.block.material Material]
           [net.minecraft.block.BlockState]
           [net.minecraft.util Direction Hand]))

(defn register-matrix []
  (let [block (proxy [Block] [(-> (Block$Properties/create Material/ROCK)
                                 (.hardnessAndResistance 3.0)
                                 (.lightValue 1))]
                (onBlockActivated [state world pos player hand direction face partial]
                  (core/handle-block-activated world pos state player))
                (onBlockPlacedBy [world pos state placer stack]
                  (core/on-block-placed world pos state placer stack)))]
    (registry/register-block block "wireless.matrix")))