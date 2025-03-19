(ns forge-impl.registry-scanner
  (:require [mcmod.protocols :refer :all]
            [mcmod.registry :as mcmod-registry]
            [forge-impl.block-adapter :as block-adapter]
            [forge-impl.item-adapter :as item-adapter]
            [forge-impl.tile-entity-adapter :as tile-adapter]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.registries ForgeRegistries]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.block Block]
           [net.minecraft.item Item BlockItem]
           [net.minecraft.tileentity TileEntityType]
           [net.minecraft.item Item$Properties]))

;; Convert mcmod registry entries to Forge implementation
(defn- create-forge-block [mcmod-block]
  (block-adapter/create-forge-block mcmod-block))

(defn- create-forge-item [mcmod-item]
  (item-adapter/create-forge-item mcmod-item))

(defn- create-forge-tile-entity [mcmod-te]
  (tile-adapter/create-forge-tile-entity mcmod-te))

;; Register converted implementations with Forge
(defn register-block! [mod-id block-id block]
  (let [forge-block (create-forge-block block)
        registry-name (ResourceLocation. mod-id block-id)]
    (log/info (str "Registering block: " block-id))
    (.setRegistryName forge-block registry-name)
    (.register ForgeRegistries/BLOCKS forge-block)
    ; Create and register BlockItem
    (let [block-item (BlockItem. forge-block (Item$Properties.))]
      (.setRegistryName block-item registry-name)
      (.register ForgeRegistries/ITEMS block-item))
    forge-block))

(defn register-item! [mod-id item-id item]
  (let [forge-item (create-forge-item item)
        registry-name (ResourceLocation. mod-id item-id)]
    (log/info (str "Registering item: " item-id))
    (.setRegistryName forge-item registry-name)
    (.register ForgeRegistries/ITEMS forge-item)
    forge-item))

(defn register-tile-entity! [mod-id te-id te]
  (let [forge-te (create-forge-tile-entity te)
        registry-name (ResourceLocation. mod-id te-id)]
    (log/info (str "Registering tile entity: " te-id))
    (.setRegistryName forge-te registry-name)
    (.register ForgeRegistries/TILE_ENTITIES forge-te)
    forge-te))

;; Main scanning function
(defn scan-and-register-mod! [mod-id]
  (log/info (str "Scanning mod content for: " mod-id))
  (let [registry (mcmod-registry/get-mod-registry mod-id)]
    (doseq [[block-id block] (mcmod-registry/get-blocks registry)]
      (register-block! mod-id block-id block))
    (doseq [[item-id item] (mcmod-registry/get-items registry)]
      (register-item! mod-id item-id item))
    (doseq [[te-id te] (mcmod-registry/get-tile-entities registry)]
      (register-tile-entity! mod-id te-id te))))