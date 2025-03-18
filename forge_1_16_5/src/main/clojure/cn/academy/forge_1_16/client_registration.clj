(ns cn.academy.forge-1-16.client-registration
  (:require [cn.academy.forge-1-16.client-bridge :as bridge])
  (:import [net.minecraftforge.fml.client ClientModLoader]
           [net.minecraftforge.fml.event.lifecycle FMLClientSetupEvent]
           [net.minecraftforge.client.event ModelRegistryEvent]))

(gen-class
  :name cn.academy.forge_1_16.ClientRegistration
  :methods [[onClientSetup [net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent] void]
            [onModelRegistration [net.minecraftforge.client.event.ModelRegistryEvent] void]]
  :prefix "client-"
  :main false)

(def client-bridge (bridge/create-bridge))

(defn client-onClientSetup [_ event]
  (bridge/setup-client client-bridge event))

(defn client-onModelRegistration [_ event]
  nil) ; Model registration now handled through bridge setup