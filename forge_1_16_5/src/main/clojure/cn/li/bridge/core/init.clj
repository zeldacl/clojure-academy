(ns cn.li.bridge.core.init
  (:require [cn.li.bridge.core.registry :as registry]
            [cn.li.bridge.block.api :as block]
            [cn.li.bridge.energy.api :as energy]
            [cn.li.bridge.network.api :as network]
            [cn.li.bridge.event.api :as event]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent FMLClientSetupEvent]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]))

(defn initialize-bridges []
  (log/info "Initializing bridges for Forge 1.16.5")
  (let [block-factory (block/create-forge-factory)
        energy-impl (energy/create-energy-impl)
        event-bridge (event/create-forge-bridge)]
    
    (registry/initialize-registry)
    (network/initialize-networking)))

(defn setup-common [event]
  (log/info "Common setup for Forge 1.16.5")
  (initialize-bridges))

(defn setup-client [event]
  (log/info "Client setup for Forge 1.16.5"))

(defn register-event-handlers []
  (let [event-bridge (event/create-forge-bridge)]
    (event/register-core-handlers event-bridge)))

(defn init []
  (log/info "Initializing Forge 1.16.5 bridge")
  (let [mod-bus (.getModEventBus (FMLJavaModLoadingContext/get))]
    (.addListener mod-bus 
                 (reify java.util.function.Consumer
                   (accept [_ event]
                     (setup-common event))))
    (.addListener mod-bus
                 (reify java.util.function.Consumer
                   (accept [_ event]
                     (setup-client event))))
    (register-event-handlers)))