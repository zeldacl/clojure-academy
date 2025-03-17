(ns cn.academy.forge-1-16-5.init
  (:require [cn.academy.core.init :as core]
            [cn.academy.core.util.logging :refer [log-info]]
            [cn.academy.forge-1-16-5.block :as blocks]
            [cn.academy.forge-1-16-5.energy :as energy])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent]))

(gen-class
  :name cn.academy.forge_1_16_5.AcademyMod
  :prefix "mod-"
  :state state
  :init init
  :constructors {[] []}
  :methods []
  :annotations [[net.minecraftforge.fml.common.Mod "academy"]])

(defn mod-init []
  [[] (atom {})])

(defn setup-handler [^FMLCommonSetupEvent event]
  (log-info "Initializing Academy Mod for Forge 1.16.5")
  (core/init! blocks/forge-block-factory energy/forge-energy-impl)
  (core/initialize!))

(defn mod-setupCommon [this event]
  (setup-handler event))