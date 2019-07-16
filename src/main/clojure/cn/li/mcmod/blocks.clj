(ns cn.li.mcmod.blocks
  (:import (net.minecraft.block Block Block$Properties SoundType)
           (net.minecraft.block.material Material)))

(defmacro defblock [block-name & args]
  (let [blockdata (apply hash-map args)]))


;(let [a (doto
;          (Block$Properties/create Material/AIR)
;          (.hardnessAndResistance 0.5 0.5)
;          (.sound SoundType/WOOD)
;          )])
