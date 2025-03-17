(ns cn.academy.block.init
  (:require [cn.academy.block.registry :as registry]
            [cn.academy.block.registry :as core-registry])
  (:import [net.minecraft.block AbstractBlock$Properties]
           [net.minecraft.block.material Material]))

(defn init-blocks! []
  (let [registry (registry/->ForgeRegistry)
        block-properties (-> (AbstractBlock$Properties/of Material/METAL)
                           (.strength 2.5)
                           (.requiresCorrectToolForDrops))]
    (core-registry/register-node-blocks! registry :1.16.5 block-properties)))