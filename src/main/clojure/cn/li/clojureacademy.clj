(ns cn.li.clojureacademy
  (:import (cpw.mods.fml.common Mod Mod$EventHandler)
           (cpw.mods.fml.common.event FMLInitializationEvent)
           (org.apache.logging.log4j LogManager))
  (:use [cn.li.utils.log :only [log-message log-warn]]))

(gen-class :name ^{Mod {:modid "clojure-academy" :name "ClojureAcademy" :version "1.0"}} cn.li.clojureacademy.core.clojureacademy
           :methods [[^{Mod$EventHandler true} init [cpw.mods.fml.common.event.FMLInitializationEvent] void]])

(defn -init [this ^FMLInitializationEvent event]
  (.info (LogManager/getLogger "ClojureAcademy") "clojure-academy"))
