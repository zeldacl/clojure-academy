(ns forge-impl.lifecycle-manager
  (:require [mcmod.lifecycle :as lifecycle]
            [mcmod.logging :as log])
  (:import [net.minecraftforge.fml.common Mod$EventBusSubscriber]
           [net.minecraftforge.fml.event.server ServerStoppingEvent]
           [net.minecraftforge.eventbus.api SubscribeEvent]))

(gen-class
  :name forge_impl.LifecycleManager
  :prefix "lifecycle-"
  :methods [[^{SubscribeEvent {}} onServerStopping [net.minecraftforge.fml.event.server.ServerStoppingEvent] void]]
  :annotations {Mod$EventBusSubscriber 
               {:modid "cljacademy" 
                :bus Mod$EventBusSubscriber$Bus/FORGE}})

(defn lifecycle-onServerStopping [this ^ServerStoppingEvent event]
  (log/info "Server stopping, shutting down mod components...")
  (lifecycle/stop-all))