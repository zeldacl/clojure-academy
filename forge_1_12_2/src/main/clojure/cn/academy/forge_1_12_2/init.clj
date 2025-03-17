(ns cn.academy.forge-1-12-2.init
  (:require [cn.academy.core.init :as core]
            [cn.academy.core.util.logging :refer [log-info]]
            [cn.academy.forge-1-12-2.block :as blocks]
            [cn.academy.forge-1-12-2.energy :as energy])
  (:import [net.minecraftforge.fml.common Mod Mod$EventHandler]
           [net.minecraftforge.fml.common.event FMLPreInitializationEvent]))

(gen-class
  :name cn.academy.forge_1_12_2.AcademyMod
  :prefix "mod-"
  :methods [[preInit [net.minecraftforge.fml.common.event.FMLPreInitializationEvent] void]]
  :annotations [[net.minecraftforge.fml.common.Mod 
                 {:modid "academy" 
                  :name "Academy Mod" 
                  :version "1.0.0"
                  :acceptedMinecraftVersions "[1.12.2]"}]])

(defn mod-preInit [this ^FMLPreInitializationEvent event]
  (log-info "Initializing Academy Mod for Forge 1.12.2")
  (core/init! blocks/forge-block-factory energy/forge-energy-impl)
  (core/initialize!))