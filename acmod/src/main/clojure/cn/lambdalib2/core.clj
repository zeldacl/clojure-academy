(ns cn.lambdalib2.core
  (:require [clojure.java.io :as io])
  (:import [net.minecraftforge.fml.common Mod EventHandler]
           [net.minecraftforge.fml.common.event FMLPreInitializationEvent FMLInitializationEvent FMLPostInitializationEvent FMLLoadCompleteEvent FMLServerStoppedEvent FMLServerStartedEvent FMLServerStoppingEvent FMLServerStartingEvent FMLServerAboutToStartEvent]
           [net.minecraftforge.fml.common.network NetworkRegistry SimpleNetworkWrapper]
           [net.minecraftforge.common MinecraftForge]
           [org.apache.logging.log4j LogManager Logger]))

(def modid "lambdalib2")
(def version "1.0.0")
(def debug true)
(def channel (NetworkRegistry/INSTANCE.newSimpleChannel modid))
(def log (atom nil))

(defn get-logger [] @log)

(defn pre-init [^FMLPreInitializationEvent event]
  (reset! log (.getModLog event))
  (.registerMessage channel NetworkEvent$MessageHandler NetworkEvent$Message 0 Side/CLIENT)
  (.registerMessage channel NetworkEvent$MessageHandler NetworkEvent$Message 1 Side/SERVER)
  (.registerMessage channel NetworkMessage$Handler NetworkMessage$Message 2 Side/CLIENT)
  (.registerMessage channel NetworkMessage$Handler NetworkMessage$Message 3 Side/SERVER)
  (.registerMessage channel MsgBlockMulti$ReqHandler MsgBlockMulti$Req 4 Side/SERVER)
  (.registerMessage channel MsgBlockMulti$Handler MsgBlockMulti 5 Side/CLIENT)
  (RegistryManager/asm_RegistrationEvent this event))

(defn init [^FMLInitializationEvent event]
  (when debug (.info @log "LambdaLib2 is running in development mode."))
  (RegistryManager/asm_RegistrationEvent this event))

(defn init-client [^FMLInitializationEvent event]
  (MinecraftForge/EVENT_BUS.register (DebugDraw.)))

(defn post-init [^FMLPostInitializationEvent event]
  (RegistryManager/asm_RegistrationEvent this event))

(defn load-complete [^FMLLoadCompleteEvent event]
  (RegistryManager/asm_RegistrationEvent this event))

(defn server-stopped [^FMLServerStoppedEvent event]
  (RegistryManager/asm_RegistrationEvent this event))

(defn server-started [^FMLServerStartedEvent event]
  (RegistryManager/asm_RegistrationEvent this event))

(defn server-stopping [^FMLServerStoppingEvent event]
  (RegistryManager/asm_RegistrationEvent this event))

(defn server-starting [^FMLServerStartingEvent event]
  (RegistryManager/asm_RegistrationEvent this event))

(defn server-about-to-start [^FMLServerAboutToStartEvent event]
  (RegistryManager/asm_RegistrationEvent this event))

(Mod. modid version)
(EventHandler. pre-init init init-client post-init load-complete server-stopped server-started server-stopping server-starting server-about-to-start)
