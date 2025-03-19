(ns cn.academy.forge.v1_16_5.init
  (:require [cn.academy.core :as core]
            [cn.academy.core.init :as core-init]
            [cn.academy.forge.v1_16_5.core :as forge-core]
            [cn.academy.forge.v1_16_5.provider :as provider]
            [cn.academy.forge.v1_16_5.matrix :as matrix]
            [cn.academy.forge.v1_16_5.bridge.registry :as registry-bridge]
            [cn.academy.forge.v1_16_5.bridge.block :as block-bridge]
            [cn.academy.forge.v1_16_5.bridge.energy :as energy-bridge]
            [cn.academy.forge.v1_16_5.bridge.client :as client-bridge]
            [cn.academy.forge.v1_16_5.bridge.network :as network-bridge]
            [cn.academy.forge.v1_16_5.bridge.event :as event-bridge]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent FMLClientSetupEvent]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]))

(defn initialize-bridges []
  (log/info "Initializing Academy Mod bridges for Forge 1.16.5")
  
  ;; Create and register bridges
  (let [block-factory (block-bridge/create-forge-factory)
        energy-impl (energy-bridge/create-energy-impl)
        event-bridge (event-bridge/create-forge-bridge)]
    
    ;; Initialize core components with bridge implementations
    (core-init/init! {:block-bridge block-factory
                      :energy-bridge energy-impl
                      :event-bridge event-bridge})
    
    ;; Initialize networking
    (network-bridge/initialize-networking)
    
    ;; Initialize registry
    (registry-bridge/initialize-registry)
    
    ;; Initialize mod provider
    (provider/init)
    
    ;; Initialize matrix blocks 
    (matrix/init-matrix)))

(defn setup-common [event]
  (log/info "Academy Mod for Forge 1.16.5 - Common Setup")
  
  ;; Initialize all bridges and core components
  (initialize-bridges)
  
  ;; Perform core initialization
  (core-init/initialize!))

(defn setup-client [event]
  (log/info "Academy Mod for Forge 1.16.5 - Client Setup")
  
  ;; Setup client side registrations (screens, renderers, etc.)
  (client-bridge/setup-client-registration event))

(defn register-event-handlers []
  (let [event-bridge (event-bridge/create-forge-bridge)]
    
    ;; Register core event handlers to listen for Forge events
    (event-bridge/register-event-handler 
     event-bridge :world-load 
     (fn [event] (core/handle-world-load (:world event))))
    
    (event-bridge/register-event-handler 
     event-bridge :world-unload 
     (fn [event] (core/handle-world-unload (:world event))))
    
    (event-bridge/register-event-handler 
     event-bridge :server-tick 
     (fn [_] (core/handle-server-tick)))
    
    (event-bridge/register-event-handler 
     event-bridge :world-tick 
     (fn [event] (core/handle-world-tick (:world event))))
    
    (event-bridge/register-event-handler 
     event-bridge :player-login 
     (fn [event] (core/handle-player-login (:player event))))
    
    (event-bridge/register-event-handler 
     event-bridge :player-logout 
     (fn [event] (core/handle-player-logout (:player event))))))

(defn register-mod-handlers [mod-bus]
  (let [event-bridge (event-bridge/create-mod-bridge mod-bus)]
    
    ;; Register setup events
    (.addListener mod-bus 
                 (reify java.util.function.Consumer
                   (accept [_ event]
                     (setup-common event))))
    
    (.addListener mod-bus
                 (reify java.util.function.Consumer
                   (accept [_ event]
                     (setup-client event))))))

(defn init []
  (log/info "Initializing Academy Mod for Forge 1.16.5")
  
  ;; Get mod event bus
  (let [mod-bus (.getModEventBus (FMLJavaModLoadingContext/get))]
    
    ;; Register event handlers for mod lifecycle events
    (register-mod-handlers mod-bus)
    
    ;; Register event handlers for game events
    (register-event-handlers)
    
    ;; Return initialization status
    {:status :success}))