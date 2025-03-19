(ns cn.academy.forge.v1_16_5.bridge.client
  (:require [cn.academy.client.core :as client])
  (:import [net.minecraftforge.fml.client ClientModLoader]
           [net.minecraftforge.fml.event.lifecycle FMLClientSetupEvent]
           [net.minecraftforge.client.model ModelLoaderRegistry]
           [net.minecraft.client.renderer RenderTypeLookup RenderType]
           [net.minecraft.util ResourceLocation]))

(defprotocol IClientBridge
  "Bridge between platform-independent client and Forge"
  (register-block-render [this block]
    "Register block render type")
  (register-model-loader [this loader]
    "Register custom model loader")
  (setup-client [this event]
    "Handle client setup"))

(defrecord ForgeClientBridge []
  IClientBridge
  (register-block-render [_ block]
    (RenderTypeLookup/setRenderLayer 
      block
      (case (client/get-render-type block)
        :solid RenderType/solid
        :translucent RenderType/translucent
        :cutout RenderType/cutout
        RenderType/solid)))
  
  (register-model-loader [_ loader]
    (when-let [loader-id (client/get-model-id loader)]
      (ModelLoaderRegistry/registerLoader
        (ResourceLocation. "academy" loader-id)
        (proxy [net.minecraftforge.client.model.IModelLoader] []
          (loadModel [ctx]
            (client/load-model loader ctx))
          (onResourceManagerReload [resource-man]
            (client/reload-resources loader resource-man))))))
  
  (setup-client [_ event]
    (.enqueueWork event
      (fn []
        (let [reg (client/create-registration)]
          (client/register-screens reg)
          (client/register-renderers reg)
          (client/register-models reg))))))

(defrecord ForgeRenderer [renderer]
  net.minecraft.client.renderer.tileentity.TileEntityRenderer
  (render [_ tile matrix-stack buffer light overlay]
    (client/render renderer
                  {:matrix matrix-stack
                   :buffer buffer 
                   :light light
                   :overlay overlay}
                  {:tile tile}))
  
  (shouldRenderOffScreen [_]
    (client/should-render-offscreen? renderer)))

(defn create-bridge []
  (->ForgeClientBridge))

(defn create-renderer [renderer]
  (->ForgeRenderer renderer))

(defn setup-client-registration [event]
  (setup-client (create-bridge) event))