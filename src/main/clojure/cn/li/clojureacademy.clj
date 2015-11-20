(ns cn.li.clojureacademy
  (:import (cpw.mods.fml.common Mod Mod$EventHandler Mod$Instance)
           (cpw.mods.fml.common.event FMLInitializationEvent FMLPreInitializationEvent FMLPostInitializationEvent FMLServerStartingEvent)
           (org.apache.logging.log4j LogManager))
  (:use [cn.li.utils.log :only [log-info log-warn]]))

(def ^{:static true Mod$Instance "clojure-academy"} instance)

(gen-class :name ^{Mod {:modid "clojure-academy" :name "ClojureAcademy" :version "1.0"}} cn.li.clojureacademy.core.clojureacademy
           :methods [[^{Mod$EventHandler true} init [cpw.mods.fml.common.event.FMLInitializationEvent] void]
                     [^{Mod$EventHandler true} preInit [cpw.mods.fml.common.event.FMLPreInitializationEvent] void]
                     [^{Mod$EventHandler true} postInit [cpw.mods.fml.common.event.FMLPostInitializationEvent] void]
                     [^{Mod$EventHandler true} serverStarting [cpw.mods.fml.common.event.FMLServerStartingEvent] void]])

(defn -init [this ^FMLInitializationEvent event]
  (log-info "clojure-academy init"))

(defn -preInit [this ^FMLPreInitializationEvent event]
  (log-info "clojure-academy preInit"))

(defn -serverStarting [this ^FMLServerStartingEvent event]
  (log-info "clojure-academy serverStarting"))

(defn -postInit [this ^FMLPostInitializationEvent event]
  (log-info "clojure-academy postInit"))
