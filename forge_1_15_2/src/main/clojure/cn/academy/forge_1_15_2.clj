(ns cn.academy.forge_1_15_2
  (:require [clojure.tools.logging :as log])
  (:import [net.minecraftforge.common MinecraftForge]
           [net.minecraftforge.fml.common.event FMLInitializationEvent FMLPreInitializationEvent FMLPostInitializationEvent FMLServerStartingEvent FMLServerStoppingEvent]
           [net.minecraftforge.fml.common.network NetworkRegistry]
           [net.minecraftforge.oredict OreDictionary]))

(defn pre-init [^FMLPreInitializationEvent event]
  (log/info "Starting AcademyCraft for Forge 1.15.2")
  (log/info "https://ac.li-dev.cn/"))

(defn init [^FMLInitializationEvent event]
  (MinecraftForge/EVENT_BUS.register this)
  (OreDictionary/registerOre "plateIron" (ItemStack. net.minecraft.init.Items/IRON_INGOT)))

(defn post-init [^FMLPostInitializationEvent event]
  (log/info "Post Initialization for Forge 1.15.2"))

(defn server-starting [^FMLServerStartingEvent event]
  (log/info "Server Starting for Forge 1.15.2"))

(defn server-stopping [^FMLServerStoppingEvent event]
  (log/info "Server Stopping for Forge 1.15.2"))