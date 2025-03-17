(ns cn.academy.block.matrix-model
  (:require [cn.academy.block.matrix-resources :as resources])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraft.client.renderer.model IModelLoader ModelLoaderRegistry]
           [net.minecraft.client.renderer.texture TextureAtlasSprite]))

(defn create-matrix-model-loader [resource-manager]
  (let [model-info (resources/load-model resource-manager)
        texture-info (resources/load-texture resource-manager)]
    (reify IModelLoader
      (^void loadModel [this location]
        (let [model-loc (ResourceLocation. (resources/get-resource-location 
                                           resource-manager 
                                           (:id model-info)))]
          (.getModel (ModelLoaderRegistry/getModelOrMissing model-loc))))
      
      (^void onResourceManagerReload [this resource-man]
        ; Reload textures if needed
        ))))