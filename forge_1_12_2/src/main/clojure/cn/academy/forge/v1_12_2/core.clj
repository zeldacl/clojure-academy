(ns cn.academy.forge.v1_12_2.core
  (:require [cn.academy.api.block :as block-api]
            [cn.academy.forge.v1_12_2.block :refer [forge-factory]])
  (:import [net.minecraftforge.fml.common Mod Mod$EventHandler]
           [net.minecraftforge.fml.common.event FMLPreInitializationEvent]))

(gen-class
  :name cn.academy.forge.v1_12_2.AcademyForgeMod
  :prefix "forge-"
  :methods [[^:static preInit [net.minecraftforge.fml.common.event.FMLPreInitializationEvent] void]]
  :annotations [[net.minecraftforge.fml.common.Mod 
                {:modid "academy", :name "Academy Craft", :version "1.0"}]])

(defn forge-preInit [_ event]
  (block-api/set-forge-factory! forge-factory))