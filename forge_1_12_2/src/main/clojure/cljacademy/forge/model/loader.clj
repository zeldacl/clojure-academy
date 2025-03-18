(ns cljacademy.forge.model.loader
  (:require [cljacademy.api.model :as model])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraft.client Minecraft]
           [net.minecraft.client.renderer.texture TextureMap]
           [net.minecraftforge.client.model ModelLoaderRegistry]))

(defrecord ForgeModelLoader []
  model/IModelLoader
  (load-model [_ path]
    (-> (ModelLoaderRegistry/getModel (ResourceLocation. path))
        .bake))
  
  (load-texture [_ path]
    (let [resource-location (ResourceLocation. path)]
      (-> (TextureMap/LOCATION_BLOCKS_TEXTURE)
          (.registerSprite resource-location))))
  
  (register-model [_ model-id model]
    (ModelLoaderRegistry/registerModel model))
  
  (register-texture [_ texture-id texture]
    (-> (Minecraft/getInstance)
        .renderEngine
        (.loadTexture (ResourceLocation. texture-id) texture))))

(defn create-model-loader []
  (->ForgeModelLoader))