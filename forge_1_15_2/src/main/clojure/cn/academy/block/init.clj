(ns cn.academy.block.init
  (:require [cn.academy.block.registry :as registry]
            [cn.academy.block.registry :as core-registry])
  (:import [net.minecraft.block Block$Properties]
           [net.minecraft.block.material Material]))

(defn init-blocks! []
  (let [registry (registry/->ForgeRegistry)
        block-properties (-> (Block$Properties/create Material/IRON)
                           (.hardnessAndResistance 2.5)
                           (.harvestLevel 1))]
    (core-registry/register-node-blocks! registry :1.15.2 block-properties)))