(ns cn.academy.block.matrix-renderer
  (:require [cn.academy.block.matrix-render-core :as render-core]
            [cn.academy.block.matrix-position :as position])
  (:import [net.minecraft.client.renderer.tileentity TileEntitySpecialRenderer]
           [net.minecraft.client.renderer GlStateManager]
           [net.minecraft.util ResourceLocation]
           [org.lwjgl.opengl GL11]))

(def ^:private matrix-texture (ResourceLocation. "academy:textures/blocks/matrix.png"))

(defprotocol IForgeMatrixRenderer
  (render-tile-at [this tile x y z partial-ticks destroyed-stage])
  (apply-transform [this transform])
  (render-model-part [this part]))

(defrecord ForgeMatrixRenderer [renderer pos-adapter]
  IForgeMatrixRenderer
  (render-tile-at [_ tile x y z partial-ticks destroyed-stage]
    (let [pos (position/from-block-pos pos-adapter (.getPos tile))]
      (GlStateManager/pushMatrix)
      (GlStateManager/translate x y z)
      
      (render-core/render-tile renderer pos partial-ticks)
      (doseq [render-info (render-core/get-render-queue renderer)]
        (let [{:keys [model part transform glow]} render-info]
          (.bindTexture Minecraft/getMinecraft matrix-texture)
          (when glow
            (GlStateManager/disableLighting))
          (when transform
            (apply-transform transform))
          (render-model-part part))
        (when (:glow render-info)
          (GlStateManager/enableLighting)))
      
      (GlStateManager/popMatrix)))
  
  (apply-transform [_ {:keys [translate rotate scale]}]
    (when translate
      (GlStateManager/translate 
        (nth translate 0) 
        (nth translate 1) 
        (nth translate 2)))
    (when rotate
      (GlStateManager/rotate 
        (nth rotate 1) 0 1 0)
      (GlStateManager/rotate 
        (nth rotate 0) 1 0 0)
      (GlStateManager/rotate 
        (nth rotate 2) 0 0 1))
    (when scale
      (GlStateManager/scale 
        (nth scale 0) 
        (nth scale 1) 
        (nth scale 2))))
  
  (render-model-part [_ part]
    (.render part 0.0625)))

(defn create-renderer [matrix model]
  (->ForgeMatrixRenderer
    (render-core/create-renderer matrix model)
    (position/create-adapter)))