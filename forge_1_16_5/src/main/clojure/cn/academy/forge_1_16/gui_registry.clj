(ns cn.academy.forge-1-16.gui-registry
  (:require [cn.academy.forge-1-16.gui-bridge :as bridge])
  (:import [net.minecraft.inventory.container ContainerType]
           [net.minecraft.client.gui.ScreenManager]
           [net.minecraftforge.fml.event.lifecycle FMLClientSetupEvent]
           [net.minecraftforge.eventbus.api SubscribeEvent]))

(def ^:private gui-bridge (bridge/create-bridge))
(def ^:private container-types (atom {}))

(defn register-gui 
  "Register a GUI with provided ID and factory function"
  [registry id factory]
  (let [type (bridge/register-type gui-bridge registry id
               (fn [window-id player world pos]
                 (when-let [gui (factory world pos)]
                   (bridge/create-container gui-bridge gui player))))]
    (swap! container-types assoc id type)
    type))

(defn register-screen
  "Register screen for container type"
  [^FMLClientSetupEvent event container-type]
  (.enqueueWork event
    #(ScreenManager/registerFactory
       container-type
       (fn [container _]
         (bridge/create-screen gui-bridge container)))))

(defn get-container-type
  "Get registered container type by ID"
  [id]
  (get @container-types id))