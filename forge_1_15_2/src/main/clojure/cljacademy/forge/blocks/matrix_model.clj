(ns cljacademy.forge.blocks.matrix-model
  (:require [cljacademy.blocks.matrix-render :as core-render])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraft.client.renderer.model IModelLoader ModelLoaderRegistry]
           [net.minecraft.client.renderer.texture TextureAtlasSprite]))

(defn create-model-loader []
  (reify IModelLoader
    (^void loadModel [this location]
      (.getModel (ModelLoaderRegistry/getModelOrMissing 
                  (ResourceLocation. "cljacademy:models/block/wireless_matrix"))))
    
    (^void onResourceManagerReload [this resource-man]
      ;; Textures are handled by texture stitch event in 1.15.2
      )))