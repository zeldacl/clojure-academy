(ns cn.academy.forge-1-15.init
  (:require [cn.academy.core.init :as core-init]
            [cn.academy.core.registry :as core-registry]
            [cn.academy.api.render :as render-api]
            [cn.academy.forge-1-15.block :as forge-block]
            [cn.academy.forge-1-15.energy :as forge-energy]
            [cn.academy.forge-1-15.render :as forge-render]
            [cn.academy.forge-1-15.registry :as forge-registry])
  (:import [net.minecraftforge.eventbus.api SubscribeEvent]
           [net.minecraftforge.event RegistryEvent$Register]
           [net.minecraft.block Block]
           [net.minecraft.tileentity TileEntityType]
           [net.minecraftforge.api.distmarker Dist OnlyIn]))

(gen-class
  :name cn.academy.forge_1_15.Init
  :prefix "init-"
  :methods [[^:static registerBlocks [net.minecraftforge.event.RegistryEvent$Register] void]
            [^:static registerTileEntities [net.minecraftforge.event.RegistryEvent$Register] void]
            [^:static clientSetup [net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent] void]])

(defn init! []
  (let [block-factory (forge-block/create-forge-factory)
        energy-impl (forge-energy/create-energy-impl)]
    (core-init/init! block-factory energy-impl)
    (core-registry/init-registry!)))

(defn init-registerBlocks [_ ^RegistryEvent$Register event]
  (forge-registry/register-blocks! event))

(defn init-registerTileEntities [_ ^RegistryEvent$Register event]
  (forge-registry/register-tile-entities! event))

(OnlyIn Dist/CLIENT)
(defn init-clientSetup [_ _]
  (render-api/set-render-impl! (forge-render/create-render-impl)))