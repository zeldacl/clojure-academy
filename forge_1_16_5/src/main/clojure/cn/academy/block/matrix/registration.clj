(ns cn.academy.block.matrix.registration
  (:require [cn.academy.block.matrix-adapter :as block]
            [cn.academy.block.matrix.tile-entity-adapter :as tile]
            [cn.academy.block.matrix.gui-adapter :as gui]
            [cn.academy.forge-1-16.energy-bridge :as energy])
  (:import [net.minecraftforge.fml.common Mod$EventBusSubscriber Mod$EventBusSubscriber$Bus]
           [net.minecraftforge.event RegistryEvent$Register]
           [net.minecraft.block Block]
           [net.minecraft.tileentity TileEntityType]
           [net.minecraft.inventory.container ContainerType]
           [net.minecraftforge.common.capabilities Capability]
           [net.minecraft.util ResourceLocation]))

(gen-class
  :name cn.academy.block.matrix.Registration
  :methods [[registerBlocks [net.minecraftforge.event.RegistryEvent$Register] void]
            [registerTileEntities [net.minecraftforge.event.RegistryEvent$Register] void]
            [registerContainers [net.minecraftforge.event.RegistryEvent$Register] void]
            [registerCapabilities [net.minecraftforge.event.RegistryEvent$Register] void]]
  :prefix "registration-"
  :main false)

(def ^:private matrix-block (atom nil))
(def ^:private matrix-tile-type (atom nil))
(def ^:private matrix-container-type (atom nil))

(defn registration-registerBlocks [_ event]
  (let [registry (.getRegistry event)
        block (block/create-adapter)]
    (reset! matrix-block block)
    (.register registry
              (-> (block/create-block block)
                  (.setRegistryName (ResourceLocation. "academy" "matrix"))))))

(defn registration-registerTileEntities [_ event]
  (let [registry (.getRegistry event)]
    (reset! matrix-tile-type
            (tile/register-type registry @matrix-block))))

(defn registration-registerContainers [_ event]
  (let [registry (.getRegistry event)]
    (reset! matrix-container-type
            (gui/register-gui registry))))

(defn registration-registerCapabilities [_ event]
  (let [registry (.getRegistry event)]
    (energy/register-capability registry)))