(ns cn.academy.forge-1-15-2.init
  (:require [cn.academy.core.init :as core]
            [cn.academy.core.util.logging :refer [log-info]]
            [cn.academy.forge-1-15-2.block :as blocks]
            [cn.academy.forge-1-15-2.energy :as energy])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent]))

(gen-class
  :name cn.academy.forge_1_15_2.AcademyMod
  :prefix "mod-"
  :state state
  :init init
  :constructors {[] []}
  :methods []
  :annotations [[net.minecraftforge.fml.common.Mod "academy"]])

(defn mod-init []
  [[] (atom {})])

(defn setup-handler [^FMLCommonSetupEvent event]
  (log-info "Initializing Academy Mod for Forge 1.15.2")
  (core/init! blocks/forge-block-factory energy/forge-energy-impl)
  (core/initialize!))

(defn mod-setupCommon [this event]
  (setup-handler event))