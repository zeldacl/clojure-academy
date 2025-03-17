(ns cn.academy.core
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import [net.minecraft.creativetab CreativeTabs]
           [net.minecraft.item ItemStack]
           [net.minecraftforge.fml.common.network NetworkRegistry]
           [net.minecraftforge.common MinecraftForge]
           [net.minecraftforge.common.config Configuration]))

(def modid "academy")
(def version "1.0.0")
(def debug true)

(def channel (NetworkRegistry/INSTANCE.newSimpleChannel modid))
(def config (atom nil))

;; Creative tab for Academy Craft items
(def creative-tab 
  (proxy [CreativeTabs] ["AcademyCraft"]
    (createIcon []
      (ItemStack. net.minecraft.init.Items/DIAMOND)))) ;; Placeholder icon

(defn init-network []
  ;; Network message registration will go here
  nil)

(defn init-config [config-file]
  (reset! config (Configuration. config-file))
  (.save @config))

(defn init []
  (log/info "Initializing AcademyCraft core module")
  (MinecraftForge/EVENT_BUS.register (proxy [Object] [])))

(defn post-init []
  (when @config
    (.save @config)))