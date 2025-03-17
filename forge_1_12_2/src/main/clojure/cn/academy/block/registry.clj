(ns cn.academy.block.registry
  (:require [cn.academy.block.registry :as core-registry]
            [cn.academy.block.node-block :refer [create-node-block]])
  (:import [net.minecraft.block Block]
           [net.minecraftforge.registries GameRegistry]
           [net.minecraft.util ResourceLocation]))

(defmethod core-registry/create-node-block :1.12.2 [_ block-type properties]
  (create-node-block block-type properties))

(defrecord ForgeRegistry []
  core-registry/IBlockRegistration
  (register-block! [_ block-id block]
    (let [registry-name (ResourceLocation. "academy" block-id)]
      (.setRegistryName block registry-name)
      (GameRegistry/register block)))
  
  (register-tile-entity! [_ block tile-type]
    ; Add tile entity registration here - different API in 1.12.2
    (GameRegistry/registerTileEntity tile-type 
                                   (ResourceLocation. "academy" 
                                                    (str (.getRegistryName block) "_te")))))