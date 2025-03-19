(ns forge-impl.client-events
  (:require [forge-impl.renderer :as renderer]
            [forge-impl.gui-registry :as gui-registry]
            [cn.academy.renderers.wireless-matrix-renderer :as matrix-renderer])
  (:import [net.minecraftforge.eventbus.api SubscribeEvent]
           [net.minecraftforge.fml.event.lifecycle FMLClientSetupEvent]
           [net.minecraftforge.api.distmarker Dist OnlyIn]))

(defn register-renderers []
  (renderer/register-renderer "wireless_matrix" matrix-renderer/render-energy-field))

(defn register-screens []
  (gui-registry/register-client-gui "wireless_matrix"))

@OnlyIn(Dist/CLIENT)
(defn handle-client-setup [^FMLClientSetupEvent event]
  (register-renderers)
  (register-screens))

(gen-class
  :name forge_impl.ClientEventHandler
  :prefix "handler-"
  :methods [[^{SubscribeEvent {}} setup [net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent] void]]
  :annotations {net.minecraftforge.fml.common.Mod$EventBusSubscriber 
               {:modid "cljacademy" 
                :value net.minecraftforge.api.distmarker.Dist/CLIENT}})

(defn handler-setup [this event]
  (handle-client-setup event))