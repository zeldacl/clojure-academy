(ns forge-impl.client.block.node-renderer
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.client.renderer.tileentity TileEntityRenderer TileEntityRendererDispatcher]
           [net.minecraft.client.renderer IRenderTypeBuffer]
           [com.mojang.blaze3d.matrix MatrixStack]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.client.renderer.texture TextureAtlasSprite]
           [net.minecraft.client.renderer RenderState RenderType]))

;; Node textures
(def ^:private node-textures
  {:basic {:off (ResourceLocation. "acmod" "textures/blocks/node_basic_off.png")
           :on [(ResourceLocation. "acmod" "textures/blocks/node_basic_on_0.png")
                (ResourceLocation. "acmod" "textures/blocks/node_basic_on_1.png")
                (ResourceLocation. "acmod" "textures/blocks/node_basic_on_2.png")
                (ResourceLocation. "acmod" "textures/blocks/node_basic_on_3.png")
                (ResourceLocation. "acmod" "textures/blocks/node_basic_on_4.png")]}
   :standard {:off (ResourceLocation. "acmod" "textures/blocks/node_standard_off.png")
              :on [(ResourceLocation. "acmod" "textures/blocks/node_standard_on_0.png")
                   (ResourceLocation. "acmod" "textures/blocks/node_standard_on_1.png")
                   (ResourceLocation. "acmod" "textures/blocks/node_standard_on_2.png")
                   (ResourceLocation. "acmod" "textures/blocks/node_standard_on_3.png")
                   (ResourceLocation. "acmod" "textures/blocks/node_standard_on_4.png")]}
   :advanced {:off (ResourceLocation. "acmod" "textures/blocks/node_advanced_off.png")
              :on [(ResourceLocation. "acmod" "textures/blocks/node_advanced_on_0.png")
                   (ResourceLocation. "acmod" "textures/blocks/node_advanced_on_1.png")
                   (ResourceLocation. "acmod" "textures/blocks/node_advanced_on_2.png")
                   (ResourceLocation. "acmod" "textures/blocks/node_advanced_on_3.png")
                   (ResourceLocation. "acmod" "textures/blocks/node_advanced_on_4.png")]}})

;; Create render type for node
(defn create-node-render-type []
  (RenderType/makeType 
    "node_overlay"
    (.format DefaultVertexFormats/POSITION_COLOR_TEX_LIGHTMAP)
    7
    256
    true
    true
    (RenderType$State/getBuilder)
    .texture
    (.lightmap)
    (.overlay)
    .transparency
    (.translucent)
    .build))

;; Node renderer implementation
(defrecord NodeRenderer [dispatcher]
  TileEntityRenderer
  (render [this tile matrix-stack buffer combined-light combined-overlay]
    (let [pos (.getPos tile)
          world (.getWorld tile)
          state (.getBlockState world pos)
          node-type (.getNodeType tile)
          enabled? (.isEnabled tile)
          energy-level (int (/ (* 4 (.getEnergy tile)) (.getMaxEnergy tile)))
          texture (if enabled?
                   (nth (get-in node-textures [node-type :on]) energy-level)
                   (get-in node-textures [node-type :off]))]
      
      ;; Set up rendering
      (.pushPose matrix-stack)
      
      ;; Render node block with texture
      (let [buffer (.getBuffer buffer (create-node-render-type))
            sprite (.getSprite (.getAtlasSpriteGetter Minecraft/getInstance texture))]
        (doto buffer
          (.vertex matrix-stack 0 0 0)
          (.uv (.getU sprite 0) (.getV sprite 0))
          (.color 1.0 1.0 1.0 1.0)
          (.lightmap combined-light)
          (.endVertex)
          ;; Add remaining vertices...
          ))
      
      (.popPose matrix-stack))))

;; Create renderer factory
(defn create-renderer-factory []
  (fn [dispatcher]
    (->NodeRenderer dispatcher)))