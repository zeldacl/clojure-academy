(ns forge-impl.registry-scanner
  (:require [mcmod.protocols :refer :all]
            [forge-impl.block-impl :refer [->ForgeBlock]]
            [forge-impl.item-impl :refer [->ForgeItem]]
            [forge-impl.tile-entity-impl :refer [->ForgeTileEntity]])
  (:import [net.minecraftforge.registries ForgeRegistries]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.block Block]
           [net.minecraft.item Item]
           [net.minecraft.tileentity TileEntityType]))

(defn register-block! [mod-id block-id block]
  (let [forge-block (->ForgeBlock block)
        registry-name (ResourceLocation. mod-id block-id)]
    (.setRegistryName block registry-name)
    (.register ForgeRegistries/BLOCKS block)
    forge-block))

(defn register-item! [mod-id item-id item]
  (let [forge-item (->ForgeItem item)
        registry-name (ResourceLocation. mod-id item-id)]
    (.setRegistryName item registry-name)
    (.register ForgeRegistries/ITEMS item)
    forge-item))

(defn register-tile-entity! [mod-id te-id tile-entity block]
  (let [forge-te (->ForgeTileEntity tile-entity)
        registry-name (ResourceLocation. mod-id te-id)
        te-type (TileEntityType/Builder/create
                 #(tile-entity)
                 (into-array Block [block]))]
    (.register ForgeRegistries/TILE_ENTITIES 
              (.build te-type registry-name))
    forge-te))

(defn register-mod-content! [mod]
  (doseq [[block-id block] (get-registered-blocks mod)]
    (register-block! (.getModId mod) block-id block))
  
  (doseq [[item-id item] (get-registered-items mod)]
    (register-item! (.getModId mod) item-id item))
  
  (doseq [[te-id te block] (get-registered-tile-entities mod)]
    (register-tile-entity! (.getModId mod) te-id te block)))