(ns cn.academy.block.registry
  (:require [cn.academy.block.registry :as core-registry]
            [cn.academy.block.node-block :refer [create-node-block]])
  (:import [net.minecraft.block Block]
           [net.minecraftforge.registries ForgeRegistries]
           [net.minecraft.util ResourceLocation]))

(defmethod core-registry/create-node-block :1.16.5 [_ block-type properties]
  (create-node-block block-type properties))

(defrecord ForgeRegistry []
  core-registry/IBlockRegistration
  (register-block! [_ block-id block]
    (let [registry-name (ResourceLocation. "academy" block-id)]
      (.setRegistryName block registry-name)
      (.register ForgeRegistries/BLOCKS block)))
  
  (register-tile-entity! [_ block tile-type]
    ; Add tile entity registration here
    ))