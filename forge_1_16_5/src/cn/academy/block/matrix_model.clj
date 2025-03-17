(ns cn.academy.block.matrix-model
  (:require [cn.academy.block.matrix-resources :as resources])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraft.client.renderer.model IModelLoader BlockModel]
           [net.minecraft.client.renderer.texture TextureAtlasSprite]
           [net.minecraftforge.client.model ModelLoader]))

(defrecord MatrixModelGeometry [model-info])

(defn create-matrix-model-loader [resource-manager]
  (let [model-info (resources/load-model resource-manager)
        texture-info (resources/load-texture resource-manager)]
    (reify IModelLoader
      (^void loadModel [this location model-load-ctx]
        (let [model-loc (ResourceLocation. (resources/get-resource-location 
                                           resource-manager 
                                           (:id model-info)))]
          (->MatrixModelGeometry model-info)))
      
      (^void onResourceManagerReload [this resource-man]
        ; Handle resource reloading in 1.16.5
        (when-let [texture-atlas (.getAtlas resource-man 
                                  (ResourceLocation. "minecraft" "textures/atlas/blocks"))]
          (.addSprite texture-atlas 
                     (ResourceLocation. (resources/get-resource-location 
                                        resource-manager 
                                        (:id texture-info))))))))))