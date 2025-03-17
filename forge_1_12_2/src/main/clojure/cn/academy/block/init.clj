(ns cn.academy.block.init
  (:require [cn.academy.block.registry :as registry]
            [cn.academy.block.registry :as core-registry])
  (:import [net.minecraft.block Block]
           [net.minecraft.block.material Material]))

(def block-properties
  {:material Material/IRON
   :hardness 2.5
   :harvest-level 1
   :harvest-tool "pickaxe"})

(defn init-blocks! []
  (let [registry (registry/->ForgeRegistry)]
    (core-registry/register-node-blocks! registry :1.12.2 block-properties)))