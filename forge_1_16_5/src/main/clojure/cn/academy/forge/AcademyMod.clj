(ns cn.academy.forge.AcademyMod
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core]
            [cn.academy.forge.adapter :as adapter]
            [cn.academy.forge.network :as network]
            [cn.academy.forge.gui :as gui]
            [cn.academy.forge.events :as events]
            [cn.academy.forge.datagen.DataGenerators :as datagen]
            [cn.mcmod.logging :as mclog])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent
                                                  FMLClientSetupEvent
                                                  FMLDedicatedServerSetupEvent]
           [net.minecraftforge.common MinecraftForge])
  (:gen-class
    :name cn.academy.forge.AcademyMod
    :prefix "mod-"
    :state state
    :init init
    :constructors {[] []}))

(def ^:const MOD-ID "acmod")

;; State atoms for bridges/adapters
(def registry-bridge (atom nil))
(def network-bridge (atom nil))
(def capability-bridge (atom nil))
(def event-bus (atom nil))
(def event-bridge (atom nil))
(def gui-handler (atom nil))

(defn mod-init []
  ;; Initialize the logger for Forge 1.16.5
  (mclog/init-logger! "Forge-1.16.5")
  [[] (atom {})])

(defn init-bridges! []
  ;; Create protocol bridges
  (reset! registry-bridge (adapter/create-registry-bridge MOD-ID))
  (reset! network-bridge (network/create-network-bridge MOD-ID))
  (reset! capability-bridge (adapter/create-capability-bridge))
  (reset! event-bus (events/create-event-bus))
  (reset! event-bridge (events/create-event-bridge @event-bus))
  (reset! gui-handler (gui/->ForgeGuiHandler))
  
  ;; Register capabilities
  (adapter/register-capabilities! @capability-bridge)
  
  ;; Initialize core with bridges
  (core/init! @registry-bridge @network-bridge @capability-bridge))

(defn setup-common! [event]
  ;; Register event handlers
  (let [mod-bus (.getModEventBus event)
        forge-bus (MinecraftForge/EVENT_BUS)]
    (events/register-forge-handlers! 
      mod-bus 
      forge-bus 
      @event-bridge))
      
  ;; Setup core common systems
  (core/setup-common!))

(defn setup-client! []
  ;; Initialize client-side handlers
  (core/setup-client!))

(defn setup-server! []
  ;; Initialize server-side handlers
  (core/setup-server!))

;; Mod lifecycle methods
(defn mod-constructor [this]
  (let [mod-bus (.get (FMLJavaModLoadingContext/get) "modEventBus")]
    ;; Register data generators
    (datagen/register-generators! mod-bus)))


(defn mod-setup [this event]
  (setup-common! event))

(defn mod-client-setup [this event]
  (setup-client!))

(defn mod-server-setup [this event]
  (setup-server!))

;; Register event handlers for mod lifecycle events
(.addListener (FMLJavaModLoadingContext/get)
  (reify Consumer
    (accept [this event]
      (when (instance? FMLCommonSetupEvent event)
        (mod-setup this event)))))


(.addListener (FMLJavaModLoadingContext/get)
  (reify Consumer 
    (accept [this event]
      (when (instance? FMLClientSetupEvent event)
        (mod-client-setup this event)))))


(.addListener (FMLJavaModLoadingContext/get)
  (reify Consumer
    (accept [this event]
      (when (instance? FMLDedicatedServerSetupEvent event)
        (mod-server-setup this event)))))