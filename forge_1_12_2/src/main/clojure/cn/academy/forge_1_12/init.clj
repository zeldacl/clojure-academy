(ns cn.academy.forge-1-12.init
  (:require [clojure.tools.logging :as log]
            [cn.academy.protocols.block :as block-api]
            [cn.academy.protocols.render :as render-api]
            [cn.academy.core.init :as core-init]
            [cn.academy.core.registry :as core-registry]
            [cn.academy.forge-1-12.block :as forge-block]
            [cn.academy.forge-1-12.energy :as forge-energy]
            [cn.academy.forge-1-12.render :as forge-render]
            [cn.academy.forge-1-12.registry :as forge-registry])
  (:import [net.minecraftforge.fml.common.eventhandler SubscribeEvent]
           [net.minecraftforge.event RegistryEvent$Register]
           [net.minecraft.block Block]
           [net.minecraft.tileentity TileEntity]
           [net.minecraftforge.fml.relauncher Side]))

(gen-class
  :name cn.academy.forge_1_12.Init
  :prefix "init-"
  :methods [[^:static registerBlocks [net.minecraftforge.event.RegistryEvent$Register] void]
            [^:static registerTileEntities [] void]
            [^:static clientSetup [] void]])

(defn init! []
  (let [block-factory (forge-block/create-forge-factory)
        energy-impl (forge-energy/create-energy-impl)]
    (core-init/init! block-factory energy-impl)
    (core-registry/init-registry!)))

(defn init-registerBlocks [_ ^RegistryEvent$Register event]
  (forge-registry/register-blocks! event))

(defn init-registerTileEntities [_]
  (forge-registry/register-tile-entities!))

(defn init-clientSetup [_]
  (render-api/set-render-impl! (forge-render/create-render-impl)))