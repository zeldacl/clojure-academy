(ns forge-impl.registry.content-scanner
  (:require [mcmod.registry :as mcmod-registry]
            [mcmod.protocols :refer :all]
            [forge-impl.block.block-adapter :as block-adapter]
            [forge-impl.container.container-adapter :as container-adapter]
            [forge-impl.gui.gui-adapter :as gui-adapter])
  (:import [net.minecraft.block Block]
           [net.minecraftforge.registries ForgeRegistries]
           [net.minecraft.util ResourceLocation]
           [net.minecraftforge.fml RegistryObject]))

;; Track registered content
(def ^:private registrations (atom {}))

;; Convert mod ID and name to ResourceLocation
(defn- create-resource-location [mod-id name]
  (ResourceLocation. mod-id name))

;; Register a block with Forge
(defn register-block! [mod-id block-id block]
  (let [forge-block (block-adapter/create-forge-block block)
        location (create-resource-location mod-id block-id)]
    (.register ForgeRegistries/BLOCKS forge-block location)
    (swap! registrations assoc-in [:blocks block-id] forge-block)
    forge-block))

;; Register a container type
(defn register-container! [mod-id container-id container]
  (let [forge-container (container-adapter/create-forge-container container)
        location (create-resource-location mod-id container-id)]
    (.register ForgeRegistries/CONTAINERS forge-container location)
    (swap! registrations assoc-in [:containers container-id] forge-container)
    forge-container))

;; Register a GUI type
(defn register-gui! [mod-id gui-id gui]
  (let [forge-gui (gui-adapter/create-forge-gui gui)
        location (create-resource-location mod-id gui-id)]
    (swap! registrations assoc-in [:guis gui-id] forge-gui)
    forge-gui))

;; Scan mcmod registry and register content with Forge
(defn scan-and-register! []
  (doseq [[mod-id registry] @mcmod-registry/registries]
    ;; Register blocks
    (doseq [[block-id block] (:blocks registry)]
      (register-block! mod-id block-id block))
    
    ;; Register containers
    (doseq [[container-id container] (:containers registry)]
      (register-container! mod-id container-id container))
    
    ;; Register GUIs
    (doseq [[gui-id gui] (:guis registry)]
      (register-gui! mod-id gui-id gui))))