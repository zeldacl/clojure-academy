(ns cljacademy.forge.blocks.matrix-render
  (:require [cljacademy.blocks.matrix-render :as core-render])
  (:import [net.minecraft.client.renderer.tileentity TileEntityRenderer]
           [com.mojang.blaze3d.matrix MatrixStack]
           [net.minecraft.client Minecraft]))

(defn create-tile-renderer []
  (proxy [TileEntityRenderer] []
    (render [tile matrix-stack buffer-in combined-light combined-overlay]
      (let [renderer (core-render/create-renderer tile)
            partial-ticks (.getPartialTickTime (Minecraft/getInstance))
            time (* partial-ticks 0.05)]
        (.push matrix-stack)
        (core-render/render-base renderer)
        (core-render/render-core renderer)
        (core-render/render-plates renderer)
        (core-render/render-shield renderer time)
        (.pop matrix-stack)))
    
    (shouldRenderOffScreen [_]
      false)))