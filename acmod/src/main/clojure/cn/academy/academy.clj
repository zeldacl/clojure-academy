(ns cn.academy.academy
  (:require [clojure.tools.logging :as log]
            [cn.academy.forge_1_12_2 :as forge-1-12-2]
            [cn.academy.forge_1_15_2 :as forge-1-15-2]
            [cn.academy.forge_1_16_5 :as forge-1-16-5])
  (:import [net.minecraft.creativetab CreativeTabs]
           [net.minecraft.item ItemStack]
           [net.minecraft.util ResourceLocation]
           [net.minecraftforge.common MinecraftForge]
           [net.minecraftforge.common.config Configuration]
           [net.minecraftforge.fml.common.Mod]
           [net.minecraftforge.fml.common.event FMLPreInitializationEvent FMLInitializationEvent FMLPostInitializationEvent FMLServerStartingEvent FMLServerStoppingEvent]
           [net.minecraftforge.fml.common.network.NetworkRegistry]
           [net.minecraftforge.oredict OreDictionary]
           [org.apache.logging.log4j.LogManager]))

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

(defn pre-init [^FMLPreInitializationEvent event]
  (log/info "Starting AcademyCraft")
  (log/info "Copyright (c) Lambda Innovation, 2013-2018")
  (log/info "https://ac.li-dev.cn/")
  (reset! config (Configuration. (.getSuggestedConfigurationFile event)))
  (.save @config)
  (forge-1-12-2/pre-init event))

(defn init [^FMLInitializationEvent event]
  (MinecraftForge/EVENT_BUS.register this)
  (OreDictionary/registerOre "plateIron" (ItemStack. net.minecraft.init.Items/IRON_INGOT))
  (forge-1-12-2/init event))

(defn post-init [^FMLPostInitializationEvent event]
  (.save @config)
  (forge-1-12-2/post-init event))

(defn server-starting [^FMLServerStartingEvent event]
  (.save @config)
  (forge-1-12-2/server-starting event))

(defn server-stopping [^FMLServerStoppingEvent event]
  (.save @config)
  (forge-1-12-2/server-stopping event))

(defn on-client-disconnection-from-server [^net.minecraftforge.fml.common.network.FMLNetworkEvent$ClientDisconnectionFromServerEvent e]
  (.save @config))

(defn debug [msg]
  (when debug
    (log/info msg)))