(ns forge-impl.content-scanner
  (:require [mcmod.registry :as mcmod]
            [forge-impl.adapters.block-adapter :as block-adapter]
            [forge-impl.adapters.item-adapter :as item-adapter]
            [forge-impl.adapters.tile-entity-adapter :as te-adapter])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraftforge.registries ForgeRegistries]
           [net.minecraft.item BlockItem Item$Properties]))

(defn- scan-mod! [mod-id]
  (println "Scanning content for mod:" mod-id)
  
  (let [blocks (mcmod/get-blocks mod-id)
        items (mcmod/get-items mod-id)
        tile-entities (mcmod/get-tile-entities mod-id)]
    
    {:blocks blocks
     :items items
     :tile-entities tile-entities}))

(defn- register-block! [mod-id id block]
  (let [forge-block (block-adapter/create-forge-block block)
        reg-name (ResourceLocation. mod-id id)]
    (.setRegistryName forge-block reg-name)
    (.register ForgeRegistries/BLOCKS forge-block)
    ; Create and register BlockItem
    (let [item-props (Item$Properties.)
          block-item (BlockItem. forge-block item-props)]
      (.setRegistryName block-item reg-name)
      (.register ForgeRegistries/ITEMS block-item))
    forge-block))

(defn- register-item! [mod-id id item]
  (let [forge-item (item-adapter/create-forge-item item)
        reg-name (ResourceLocation. mod-id id)]
    (.setRegistryName forge-item reg-name)
    (.register ForgeRegistries/ITEMS forge-item)))

(defn- register-tile-entity! [mod-id id te-def block]
  (let [te-type (te-adapter/create-tile-entity-type te-def block)
        reg-name (ResourceLocation. mod-id id)]
    (.setRegistryName te-type reg-name)
    (.register ForgeRegistries/TILE_ENTITIES te-type)))

(defn register-mod-content! [^String mod-id]
  (try
    (let [{:keys [blocks items tile-entities]} (scan-mod! mod-id)
          registered-blocks (atom {})]
      
      ; Register blocks first
      (doseq [[id block] blocks]
        (let [forge-block (register-block! mod-id id block)]
          (swap! registered-blocks assoc id forge-block)))
      
      ; Register items
      (doseq [[id item] items]
        (register-item! mod-id id item))
      
      ; Register tile entities, linking them to their blocks
      (doseq [[id {:keys [block-id properties]}] tile-entities
              :let [block (get @registered-blocks block-id)]]
        (when block
          (register-tile-entity! mod-id id properties block))))
    (catch Exception e
      (println "Error registering content for mod" mod-id ":" (.getMessage e))
      (.printStackTrace e))))