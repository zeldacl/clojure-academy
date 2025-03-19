(ns cn.academy.blocks.helpers
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.block.material Material]))

(defn create-block-base
  "Creates a base block with default properties"
  [& {:keys [material hardness resistance light-level]
      :or {material Material/ROCK
           hardness 3.0
           resistance 5.0
           light-level 0}}]
  (reify IBlock
    (get-properties [this]
      {:material material
       :hardness hardness
       :resistance resistance
       :light-level light-level})
    
    (get-material [this] material)
    (get-hardness [this] hardness)
    (get-resistance [this] resistance)
    (get-light-level [this] light-level)
    
    (on-activated [this pos data] true)
    (on-placed [this pos data])
    (on-removed [this pos])))