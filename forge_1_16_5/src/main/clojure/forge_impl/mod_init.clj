(ns forge-impl.mod-init
  (:require [mcmod.logging :as log]
            [mcmod.concurrent :as concurrent]
            [mcmod.lifecycle :as lifecycle])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent]
           [net.minecraftforge.eventbus.api SubscribeEvent]))

(gen-class
  :name forge_impl.ModInit
  :prefix "init-"
  :methods [[^{SubscribeEvent {}} onCommonSetup [net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent] void]]
  :annotations {Mod$EventBusSubscriber 
               {:modid "cljacademy" 
                :bus Mod$EventBusSubscriber$Bus/MOD}})

(defn init-onCommonSetup [this ^FMLCommonSetupEvent event]
  (log/info "Initializing ClojureAcademy mod...")
  (concurrent/init-monitoring)
  (lifecycle/start-all)
  (log/info "ClojureAcademy mod initialized successfully"))