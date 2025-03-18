(ns cn.academy.block.matrix.client-registration
  (:require [cn.academy.block.matrix.gui-registry :as gui]
            [cn.academy.block.matrix-model :as model]
            [cn.academy.block.matrix-render :as render])
  (:import [net.minecraftforge.fml.client ClientModLoader]
           [net.minecraftforge.fml.event.lifecycle FMLClientSetupEvent]
           [net.minecraftforge.client.model ModelLoaderRegistry]
           [net.minecraft.client.renderer RenderTypeLookup RenderType]))

(gen-class
  :name cn.academy.block.matrix.ClientRegistration
  :methods [[onClientSetup [net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent] void]
            [onModelRegistration [net.minecraftforge.client.event.ModelRegistryEvent] void]]
  :prefix "client-"
  :main false)

(defn client-onClientSetup [_ event]
  (.enqueueWork event
    (fn []
      (gui/register-screens)
      (RenderTypeLookup/setRenderLayer 
        matrix-block 
        RenderType/translucent))))

(defn client-onModelRegistration [_ _]
  (ModelLoaderRegistry/registerLoader
    (ResourceLocation. "academy" "matrix_model")
    (model/create-model-loader)))