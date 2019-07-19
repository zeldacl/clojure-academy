(ns cn.li.academy.energy.blocks.node
  (:require [cn.li.mcmod.blocks :refer [defblock]])
  (:import (net.minecraft.block Block)
           (net.minecraft.block.material Material)))

(defblock block-node
  :container? true
  :states {:type :unknown
           ;:connected [:true :false]
           ;:energy    [0 1 2 3 4]
           }
  ;:override {:create-new-tile-entity new-tile-block-entity
  ;           :on-block-activated     on-tile-block-click}
  :properties {
               :connected [:bool]
               :energy [:integer 0 4]
               }
  :attributes {;:creative-tab ""
               :material      Material/ROCK
               :hardness      (float 2.5)
               ;:step-sound Block/soundTypeStone
               :registry-name ""
               :harvest-level ["pickaxe", 1]}
  ;:override {:create-new-tile-entity new-tile-block-entity
  ;           :on-block-activated     on-tile-block-click}


  ;:creative-tab CreativeTabs/tabBlock
  )

;(defblock test-block
;  :hardness 0.5
;  :creative-tab CreativeTabs/tabBlock
;  :light-level (float 1.0)
;  :step-sound Block/soundTypeStone)
