(ns cn.academy.forge-1-12.render
  (:require [cn.academy.api.render :as render-api])
  (:import [net.minecraft.client.renderer.texture TextureMap]
           [net.minecraft.client.renderer GlStateManager]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.client Minecraft]))

(defn create-render-impl []
  {:matrix-stack GlStateManager
   :translate (fn [x y z]
               (GlStateManager/translate x y z))
   :bind-texture (fn [^ResourceLocation texture]
                  (.bindTexture (Minecraft/getMinecraft) texture))
   :render-model (fn [model]
                  (.renderAll model))
   :load-model (fn [path]
                (net.minecraftforge.client.model.ModelLoader/loadModel
                  (ResourceLocation. path)))
   :load-texture (fn [path]
                  (ResourceLocation. path))})