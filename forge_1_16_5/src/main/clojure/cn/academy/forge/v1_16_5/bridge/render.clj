(ns cn.academy.forge.v1_16_5.bridge.render
  (:require [cn.academy.render.core :as render]
            [cn.academy.client.core :as client])
  (:import [net.minecraft.client.renderer.tileentity TileEntityRenderer TileEntityRendererDispatcher]
           [net.minecraft.client.renderer RenderType IRenderTypeBuffer]
           [net.minecraft.client.renderer.model ModelRenderer]
           [net.minecraft.util ResourceLocation]
           [com.mojang.blaze3d.matrix MatrixStack]
           [com.mojang.blaze3d.vertex IVertexBuilder]
           [net.minecraft.client.renderer.texture TextureAtlasSprite]))

(defprotocol IRenderBridge
  "Bridge between platform-independent rendering and Forge"
  (create-renderer [this renderer-def]
    "Create a Forge renderer from platform renderer definition")
  (register-renderer [this registry id renderer]
    "Register the renderer with the registry")
  (bind-texture [this location]
    "Bind a texture for rendering")
  (create-render-type [this params]
    "Create a RenderType based on given parameters"))

(deftype ForgeRenderAdapter [renderer-def]
  TileEntityRenderer
  (render [_ tile matrix-stack buffer combinedLight combinedOverlay]
    (let [pos (.getBlockPos tile)
          render-ctx {:pos {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}
                      :matrix matrix-stack
                      :buffer buffer
                      :light combinedLight
                      :overlay combinedOverlay}]
      (render/render renderer-def render-ctx)))
  
  (shouldRenderOffScreen [_]
    (render/render-offscreen? renderer-def)))

(deftype ForgeRenderBridge []
  IRenderBridge
  (create-renderer [_ renderer-def]
    (->ForgeRenderAdapter renderer-def))
  
  (register-renderer [_ registry id renderer]
    (registry/register-renderer registry id renderer))
  
  (bind-texture [_ location]
    (let [resource-location (ResourceLocation. "academy" location)]
      (client/bind-texture resource-location)))
  
  (create-render-type [_ params]
    (case (:type params)
      :translucent (RenderType/translucent)
      :cutout (RenderType/cutout)
      :cutout-mipped (RenderType/cutoutMipped)
      :solid (RenderType/solid)
      (RenderType/solid))))

(defn create-bridge []
  (->ForgeRenderBridge))

(defn create-vertex-builder [buffer render-type]
  (.getBuffer buffer render-type))

(defn create-matrix-stack []
  (MatrixStack.))

(defn push-matrix [matrix]
  (.pushPose matrix))

(defn pop-matrix [matrix]
  (.popPose matrix))

(defn translate [matrix x y z]
  (.translate matrix x y z))

(defn rotate [matrix angle x y z]
  (.mulPose matrix (com.mojang.math.Quaternion. x y z angle)))

(defn scale [matrix x y z]
  (.scale matrix x y z))

;; Bridge function to create a model renderer
(defn create-model-renderer []
  (ModelRenderer. nil 0 0))

;; Function to get texture from sprite location
(defn get-sprite [sprite-location]
  (let [resource-location (ResourceLocation. "academy" sprite-location)]
    (-> (net.minecraft.client.Minecraft/getInstance)
        .getTextureAtlas
        (.getSprite resource-location))))