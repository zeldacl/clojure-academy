(ns forge-impl.registration-manager
  (:require [mcmod.registry :as mcmod]
            [forge-impl.adapters.block-adapter :as blocks]
            [forge-impl.adapters.item-adapter :as items]
            [forge-impl.adapters.tile-entity-adapter :as tiles])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraftforge.registries ForgeRegistries]
           [net.minecraft.item BlockItem Item$Properties]))

(defn- register-block! [mod-id block-id block-impl]
  (let [block (blocks/create-forge-block block-impl)
        registry-name (ResourceLocation. mod-id block-id)]
    (.setRegistryName block registry-name)
    (.register ForgeRegistries/BLOCKS block)
    ; Create and register BlockItem
    (let [block-item (BlockItem. block (Item$Properties.))]
      (.setRegistryName block-item registry-name)
      (.register ForgeRegistries/ITEMS block-item))
    block))

(defn- register-item! [mod-id item-id item-impl]
  (let [item (items/create-forge-item item-impl)
        registry-name (ResourceLocation. mod-id item-id)]
    (.setRegistryName item registry-name)
    (.register ForgeRegistries/ITEMS item)
    item))

(defn- register-tile-entity! [mod-id te-id te-impl block]
  (let [te-type (tiles/create-tile-entity-type te-impl block)
        registry-name (ResourceLocation. mod-id te-id)]
    (.setRegistryName te-type registry-name)
    (.register ForgeRegistries/TILE_ENTITIES te-type)
    te-type))

(defn register-mod-content! [mod-id]
  (let [blocks (mcmod/get-blocks mod-id)
        items (mcmod/get-items mod-id)
        tile-entities (mcmod/get-tile-entities mod-id)]
    
    ; Register blocks first
    (let [registered-blocks 
          (into {}
                (for [[id impl] blocks]
                  [id (register-block! mod-id id impl)]))]
      
      ; Register standalone items
      (doseq [[id impl] items]
        (register-item! mod-id id impl))
      
      ; Register tile entities, referencing their blocks
      (doseq [[id {:keys [block-id properties]}] tile-entities
              :let [block (get registered-blocks block-id)]]
        (when block
          (register-tile-entity! mod-id id properties block)))))