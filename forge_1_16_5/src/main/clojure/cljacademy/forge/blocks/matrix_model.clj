(ns cljacademy.forge.blocks.matrix-model
  (:require [cljacademy.blocks.matrix-render :as core-render])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraft.client.renderer.model IModelLoader]
           [net.minecraft.client.renderer.texture TextureAtlasSprite]
           [net.minecraftforge.client.model ModelLoader]))

(defrecord MatrixModelGeometry []
  Object
  (bake [this ctx state formats]
    (let [model (ModelLoader/loadModel 
                  (ResourceLocation. "cljacademy:models/block/wireless_matrix"))]
      (.bake model ctx state formats))))

(defn create-model-loader []
  (reify IModelLoader
    (^void loadModel [this location model-load-ctx]
      (->MatrixModelGeometry))
    
    (^void onResourceManagerReload [this resource-man]
      (when-let [texture-atlas (.getAtlas resource-man 
                                (ResourceLocation. "minecraft" "textures/atlas/blocks"))]
        (.addSprite texture-atlas 
                   (ResourceLocation. "cljacademy:textures/blocks/wireless_matrix"))))))