(ns forge-impl.event-handler
  (:require [forge-impl.block-converter :as converter]
            [mcmod.protocols :refer :all]
            [forge-impl.registry-scanner :as scanner])
  (:import [net.minecraftforge.eventbus.api SubscribeEvent]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent]
           [net.minecraftforge.fml.common Mod$EventBusSubscriber]
           [net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus MOD]))

(defn handle-setup [^FMLCommonSetupEvent event]
  (let [registry (scanner/scan-and-register)]
    (println "ClojureAcademy Mod Setup Complete")))

(gen-class
  :name forge_impl.EventHandler
  :prefix "handler-"
  :methods [[^{SubscribeEvent {}} setup [net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent] void]]
  :annotations {net.minecraftforge.fml.common.Mod$EventBusSubscriber 
                {:modid "cljacademy" :bus net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus/MOD}})

(defn handler-setup [this event]
  (handle-setup event))