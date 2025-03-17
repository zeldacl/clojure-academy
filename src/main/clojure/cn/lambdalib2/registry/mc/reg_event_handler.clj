(ns cn.lambdalib2.registry.mc.reg_event_handler
  (:require [net.minecraftforge.common :refer [MinecraftForge]]
            [net.minecraftforge.fml.common.event :refer [FMLPreInitializationEvent]]
            [cn.lambdalib2.util :refer [Debug ReflectionUtils]]))

(defn- pre-init [^FMLPreInitializationEvent ev]
  (doseq [field (ReflectionUtils/getFields RegEventHandler)]
    (try
      (let [obj (.get field nil)]
        (Debug/assertNotNull obj)
        (MinecraftForge/EVENT_BUS/register obj))
      (catch Exception e
        (throw (RuntimeException. e))))))

(defn register-event-handler [handler]
  (MinecraftForge/EVENT_BUS/register handler))

(defn unregister-event-handler [handler]
  (MinecraftForge/EVENT_BUS/unregister handler))