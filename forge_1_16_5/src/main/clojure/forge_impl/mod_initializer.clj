(ns forge-impl.mod-initializer
  (:require [forge-impl.registry.registry-scanner :as registry-scanner]
            [forge-impl.registry.dependency-scanner :as dependency-scanner]
            [forge-impl.network.gui-network :as gui-network]
            [forge-impl.client.registry-handler :as client-registry]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]
           [net.minecraftforge.fml DistExecutor]
           [net.minecraftforge.api.distmarker Dist]
           [net.minecraftforge.eventbus.api SubscribeEvent]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent FMLClientSetupEvent]))

;; Client-side initialization
(defn init-client! []
  (log/info "Starting client initialization")
  (client-registry/init-client!)
  (log/info "Client initialization complete"))

;; Common initialization
(defn init-common! []
  (log/info "Starting common initialization")
  (dependency-scanner/scan-dependencies!)
  (registry-scanner/scan-and-register-mods!)
  (gui-network/register-messages!)
  (log/info "Common initialization complete"))

;; Event handlers
(defn handle-common-setup! [event]
  (init-common!))

(defn handle-client-setup! [event]
  (init-client!))

;; Mod class
(gen-class
  :name forge_impl.ModInitializer
  :prefix "mod-"
  :state state
  :init init
  :constructors {[] []}
  :methods [[handleCommonSetup [net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent] void]
            [handleClientSetup [net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent] void]]
  :annotations [[net.minecraftforge.fml.common.Mod "forge_1_16_5"]])

(defn mod-init []
  [[] (atom {})])

(defn mod-handleCommonSetup [this event]
  (handle-common-setup! event))

(defn mod-handleClientSetup [this event]
  (DistExecutor/runWhenOn 
    Dist/CLIENT
    (reify DistExecutor$Runnable
      (run [_]
        (handle-client-setup! event)))))