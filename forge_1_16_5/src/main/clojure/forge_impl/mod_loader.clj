(ns forge-impl.mod-loader
  (:require [forge-impl.registry-scanner :as scanner]
            [forge-impl.block-adapter :as block-adapter]
            [forge-impl.item-adapter :as item-adapter]
            [forge-impl.capability-impl :as cap-impl]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent]
           [net.minecraftforge.eventbus.api SubscribeEvent]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]))

(def ^:private initialized (atom false))

;; Initialize our Forge adapters and scan for mcmod content
(defn- init-mod []
  (when (compare-and-set! initialized false true)
    (log/info "Initializing Forge adapter for mcmod content")
    
    ;; Set up adapters
    (block-adapter/init!)
    (item-adapter/init!)
    (cap-impl/init!)
    
    ;; Scan and register content from mcmod
    (scanner/scan-and-register-mod! "acmod")))

;; Event handler for mod initialization
(defn handle-setup [^FMLCommonSetupEvent event]
  (init-mod))

;; Register our event handlers
(defn register-handlers []
  (let [bus (.get (FMLJavaModLoadingContext/get) "modEventBus")]
    (.addListener bus (reify Consumer
                       (accept [_ event]
                         (when (instance? FMLCommonSetupEvent event)
                           (handle-setup event)))))))

;; Export a static method for registering mod handlers
(gen-class
  :name forge_impl.ModLoader
  :methods [^:static [registerHandlers [] void]]
  :prefix "loader-")

(defn loader-registerHandlers []
  (register-handlers))