(ns cn.academy.block.matrix-model
  (:require [cn.academy.block.matrix-resources :as resources])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraft.client.renderer.texture TextureMap]
           [net.minecraftforge.client.model IModel]))

(defn create-matrix-model-loader [resource-manager]
  (let [model-info (resources/load-model resource-manager)
        texture-info (resources/load-texture resource-manager)]
    (reify IModel
      (^void getTextures [this]
        [(ResourceLocation. (resources/get-resource-location resource-manager (:id texture-info)))])
      
      (^IModel retexture [this textures]
        this)
      
      (^IModel process [this state]
        this))))