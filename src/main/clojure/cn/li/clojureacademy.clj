(ns cn.li.clojureacademy
  (:import (cpw.mods.fml.common Mod Mod$EventHandler Mod$Instance)
           (cpw.mods.fml.common.event FMLInitializationEvent FMLPreInitializationEvent FMLPostInitializationEvent FMLServerStartingEvent)
           (org.apache.logging.log4j LogManager)
           (cpw.mods.fml.common.network NetworkRegistry)
           (net.minecraft.item Item))
  (:use [cn.li.utils.log :only [log-info log-warn]])
  (:use [cn.li.forge-api.utils :only [register]])
  (:use [cn.li.academy.energy.blocks :only [block-node-basic]])
  )

(def ^{:static true Mod$Instance "clojure-academy"} instance nil)
(defn bind-instance! [binding] (alter-var-root #'instance #(identity %2) binding))

(def ^{:static true} network-handle nil)
(defn bind-network-handle! [binding] (alter-var-root #'network-handle #(identity %2) binding))

(gen-class :name ^{Mod {:modid "clojure-academy" :name "ClojureAcademy" :version "1.0"}} cn.li.clojureacademy.core.clojureacademy
           :methods [[^{Mod$EventHandler true} init [cpw.mods.fml.common.event.FMLInitializationEvent] void]
                     [^{Mod$EventHandler true} preInit [cpw.mods.fml.common.event.FMLPreInitializationEvent] void]
                     [^{Mod$EventHandler true} postInit [cpw.mods.fml.common.event.FMLPostInitializationEvent] void]
                     [^{Mod$EventHandler true} serverStarting [cpw.mods.fml.common.event.FMLServerStartingEvent] void]])

(defn -init [this ^FMLInitializationEvent event]
  (log-info "clojure-academy init"))

(defn -preInit [this ^FMLPreInitializationEvent event]
  (log-info "clojure-academy preInit")
  (bind-instance! this)
  (bind-network-handle! (.newSimpleChannel NetworkRegistry/INSTANCE "clojure-academy-network"))
  (register (construct-proxy block-node-basic))
  )

(defn -serverStarting [this ^FMLServerStartingEvent event]
  (log-info "clojure-academy serverStarting"))

(defn -postInit [this ^FMLPostInitializationEvent event]
  (log-info "clojure-academy postInit"))
