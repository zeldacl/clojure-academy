(ns forge-impl.lifecycle-manager
  (:require [mcmod.lifecycle :as lifecycle]
            [mcmod.logging :as log]
            [forge-impl.event-adapters :as event-adapters]
            [mcmod.protocols :refer [on-stopping on-started]]))

(def server-events (atom nil))

(defn init-lifecycle-manager [event-bus]
  (reset! server-events (event-adapters/create-server-events event-bus)))

(gen-class
  :name forge_impl.LifecycleManager
  :prefix "lifecycle-"
  :methods [[^{net.minecraftforge.eventbus.api.SubscribeEvent {}} onServerStopping [net.minecraftforge.fml.event.server.ServerStoppingEvent] void]
            [^{net.minecraftforge.eventbus.api.SubscribeEvent {}} onServerStarted [net.minecraftforge.fml.event.server.ServerStartedEvent] void]]
  :annotations {net.minecraftforge.fml.common.Mod$EventBusSubscriber 
               {:modid "cljacademy" 
                :bus net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus/FORGE}})

(defn lifecycle-onServerStopping [this event]
  (log/info "Server stopping, shutting down mod components...")
  (on-stopping @server-events)
  (lifecycle/stop-all))

(defn lifecycle-onServerStarted [this event]
  (log/info "Server started, initializing mod components...")
  (on-started @server-events))