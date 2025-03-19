(ns cn.academy.forge.v1_16_5.core
  (:require [cn.academy.core.util.logging :refer [log-info]]
            [cn.academy.forge.v1_16_5.init :as init]
            [cn.academy.forge.v1_16_5.bridge.registry :as registry-bridge])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.eventbus.api SubscribeEvent]
           [net.minecraftforge.event RegistryEvent$Register]
           [net.minecraft.block Block]
           [net.minecraft.tileentity TileEntityType]))

(gen-class
  :name cn.academy.forge.v1_16_5.AcademyCraft
  :state state
  :init init
  :constructors {[] []}
  :prefix "mod-"
  :methods [[^:static registerBlocks [net.minecraftforge.event.RegistryEvent$Register] void]
            [^:static registerTileEntities [net.minecraftforge.event.RegistryEvent$Register] void]]
  :annotations [[net.minecraftforge.fml.common.Mod "academy"]
                [net.minecraftforge.fml.common.Mod$EventBusSubscriber {}]])

(defn mod-init []
  (log-info "Initializing Academy Mod for Forge 1.16.5")
  
  ;; Initialize all components
  (init/init)
  
  [[] (atom {})])

;; Static event handlers
(defn mod-registerBlocks [_ ^RegistryEvent$Register event]
  (registry-bridge/register-blocks! event))

(defn mod-registerTileEntities [_ ^RegistryEvent$Register event]
  (registry-bridge/register-tile-entities! event))