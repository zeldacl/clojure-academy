(ns cljacademy.forge.blocks.matrix-render
  (:require [cljacademy.blocks.matrix-render :as core-render])
  (:import [net.minecraft.client.renderer.tileentity TileEntitySpecialRenderer]
           [net.minecraft.client.renderer GlStateManager]
           [net.minecraft.client Minecraft]))

(defn create-tile-renderer []
  (proxy [TileEntitySpecialRenderer] []
    (renderTileEntityAt [tile x y z partial-ticks destroyed-stage]
      (let [renderer (core-render/create-renderer tile)
            time (* partial-ticks 0.05)]
        (GlStateManager/pushMatrix)
        (GlStateManager/translate x y z)
        (core-render/render-base renderer)
        (core-render/render-core renderer)
        (core-render/render-plates renderer)
        (core-render/render-shield renderer time)
        (GlStateManager/popMatrix)))))