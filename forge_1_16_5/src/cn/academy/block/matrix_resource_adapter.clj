(ns cn.academy.block.matrix-resource-adapter
  (:require [cn.academy.block.matrix-resources :as resources])
  (:import [net.minecraft.client.renderer.model ModelResourceLocation]
           [net.minecraft.util ResourceLocation]
           [net.minecraftforge.client.model ModelLoader]))

(defprotocol IForgeResourceAdapter
  (register-model [this])
  (register-texture [this])
  (get-model [this])
  (get-texture [this]))

(defrecord ForgeResourceAdapter [resource-manager]
  IForgeResourceAdapter
  (register-model [_]
    (let [{:keys [location data]} (resources/load-model resource-manager)]
      (.bakery ModelLoader/addSpecialModel 
               (ModelResourceLocation. location "inventory")
               data)))
  
  (register-texture [_]
    (let [{:keys [location data]} (resources/load-texture resource-manager)]
      (.register TextureMap/LOCATION_BLOCKS_TEXTURE
                 (ResourceLocation. location)
                 (TextureAtlasSprite. data))))
  
  (get-model [_]
    (let [location (resources/get-model-location resource-manager)]
      (ModelLoader/getModel (ModelResourceLocation. location "inventory"))))
  
  (get-texture [_]
    (let [location (resources/get-texture-location resource-manager)]
      (.getAtlasSprite Minecraft/getInstance
                       (ResourceLocation. location)))))

(defn create-adapter [resource-manager]
  (->ForgeResourceAdapter resource-manager))

(defn register-resources [resource-manager]
  (let [adapter (create-adapter resource-manager)]
    (register-model adapter)
    (register-texture adapter)
    adapter))