(ns cljacademy.forge.model.loader
  (:require [cljacademy.api.model :as model])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraft.client Minecraft]
           [net.minecraft.client.renderer.model ModelBakery]
           [net.minecraft.client.renderer.texture TextureAtlasSprite]))

(defrecord ForgeModelLoader []
  model/IModelLoader
  (load-model [_ path]
    (-> (Minecraft/getInstance)
        .getModelManager
        (.getModel (ResourceLocation. path))))
  
  (load-texture [_ path]
    (-> (Minecraft/getInstance)
        .getTextureManager
        (.register (ResourceLocation. path)
                   (TextureAtlasSprite/makeAtlasSprite (ResourceLocation. path)))))
  
  (register-model [_ model-id model]
    (.putModel (ModelBakery/instance)
               (ResourceLocation. model-id)
               model))
  
  (register-texture [_ texture-id texture]
    (-> (Minecraft/getInstance)
        .getTextureManager
        (.register (ResourceLocation. texture-id) texture))))

(defn create-model-loader []
  (->ForgeModelLoader))