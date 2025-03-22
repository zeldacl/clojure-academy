(ns cn.academy.forge-1-15.render
  (:require [cn.academy.protocols.render :as render-api])
  (:import [com.mojang.blaze3d.matrix MatrixStack]
           [net.minecraft.client Minecraft]
           [net.minecraft.util ResourceLocation]
           [net.minecraftforge.client.model ModelLoader]))

(defn create-render-impl []
  (let [matrix-stack (MatrixStack.)]
    {:matrix-stack matrix-stack
     :translate (fn [x y z]
                 (.translate matrix-stack x y z 1.0))
     :bind-texture (fn [^ResourceLocation texture]
                    (.bindTexture (.-textureManager (Minecraft/getInstance)) texture))
     :render-model (fn [model]
                    (.render model matrix-stack nil))
     :load-model (fn [path]
                  (ModelLoader/loadModel
                    (ResourceLocation. path)))
     :load-texture (fn [path]
                    (ResourceLocation. path))}))