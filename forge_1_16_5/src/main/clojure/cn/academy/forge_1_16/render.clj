(ns cn.academy.forge-1-16.render
  (:require [cn.academy.api.render :as render-api])
  (:import [com.mojang.blaze3d.matrix MatrixStack]
           [net.minecraft.client Minecraft]
           [net.minecraft.util ResourceLocation]
           [net.minecraftforge.client.model ModelLoader]
           [com.mojang.blaze3d.systems RenderSystem]))

(defn create-render-impl []
  (let [matrix-stack (MatrixStack.)]
    {:matrix-stack matrix-stack
     :translate (fn [x y z]
                 (.translate matrix-stack x y z))
     :bind-texture (fn [^ResourceLocation texture]
                    (RenderSystem/enableTexture)
                    (.bindTexture (.-textureManager (Minecraft/getInstance)) texture))
     :render-model (fn [model]
                    (.render model matrix-stack nil 
                            (com.mojang.blaze3d.vertex.IVertexBuilder/getBuffer)))
     :load-model (fn [path]
                  (ModelLoader/loadModel
                    (ResourceLocation. path)))
     :load-texture (fn [path]
                    (ResourceLocation. path))}))