(ns cn.li.academy.energy.blocks.node
  (:require [cn.li.mcmod.blocks :refer [defblock]])
  (:import (net.minecraft.block Block)))

(defblock block-node
  :container? true
  :block-state {:connected [:true :false]
                :energy    [0 1 2 3 4]}
  ;:override {:create-new-tile-entity new-tile-block-entity
  ;           :on-block-activated     on-tile-block-click}
  :properties {:hardness 0.5
               :step-sound Block/soundTypeStone
               :registry-name ""}


  ;:creative-tab CreativeTabs/tabBlock
  )

;(defblock test-block
;  :hardness 0.5
;  :creative-tab CreativeTabs/tabBlock
;  :light-level (float 1.0)
;  :step-sound Block/soundTypeStone)
