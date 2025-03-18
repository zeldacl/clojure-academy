(ns cn.academy.energy.api.wireless-registry
  (:require [cn.academy.energy.capability.wireless-node-capability :as node-cap]
            [cn.academy.energy.impl.wireless-system :as system]
            [cn.academy.energy.capability.wireless-capability-provider :as cap-provider]
            [cn.academy.energy.capability.wireless-capability-handler :as cap-handler])
  (:import [net.minecraftforge.common MinecraftForge]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent]
           [net.minecraftforge.eventbus.api SubscribeEvent IEventBus]))

(defn register-capabilities! []
  (node-cap/register!)
  (system/init!))

(defn register-event-handlers! [event-bus]
  (let [sys (system/get-instance)]
    (.register MinecraftForge/EVENT_BUS sys)
    (.register MinecraftForge/EVENT_BUS (cap-handler/create))
    (.register event-bus sys)))

(defn create-node-provider [node]
  (cap-provider/create-node-provider node))

(defn create-matrix-provider [matrix]
  (cap-provider/create-matrix-provider matrix))

@SubscribeEvent
(defn on-common-setup [^FMLCommonSetupEvent event]
  (register-capabilities!))