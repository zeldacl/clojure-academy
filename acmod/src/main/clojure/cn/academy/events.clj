(ns cn.academy.events
  (:require [cn.academy.core :as core]
            [cn.academy.registry :as registry]
            [mcmod.network :as network])
  (:import [net.minecraftforge.eventbus.api SubscribeEvent]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent]))

(defn setup-network []
  (let [net-registry (network/create-registry)]
    ;; Register our energy sync packet
    (network/register-packet net-registry 
                          "energy_sync" 
                          cn.academy.network.energy_sync_packet.EnergySyncPacket
                          #(println "Energy synced:" %))))

(defn handle-common-setup [^FMLCommonSetupEvent event]
  (setup-network)
  (core/init-mod))

(gen-class
  :name cn.academy.EventHandler
  :prefix "handler-"
  :methods [[^{SubscribeEvent {}} setup [net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent] void]]
  :state state)

(defn handler-setup [this event]
  (handle-common-setup event))