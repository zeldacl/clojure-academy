(ns cn.academy.forge-1-12.registry
  (:require [cn.academy.core.registry :as core-registry])
  (:import [net.minecraftforge.event RegistryEvent$Register]
           [net.minecraftforge.fml.common.registry GameRegistry]
           [net.minecraft.block Block]
           [net.minecraft.tileentity TileEntity]))

(defn create-block-instance [block-id block]
  (doto (.setRegistryName (proxy [Block] [(:material block)]
                           (createTileEntity [world state]
             (when-let [factory (core-registry/get-tile-entity-factory block-id)]
               (factory))))
        (.setUnlocalizedName (str "ac." block-id))))

(defn register-blocks! [^RegistryEvent$Register event]
  (let [registry (.getRegistry event)]
    (doseq [[block-id block] (:blocks @core-registry/registry-state)]
      (.register registry (create-block-instance block-id block)))))

(defn register-tile-entities! []
  (doseq [[tile-id factory] (:tile-entities @core-registry/registry-state)]
    (GameRegistry/registerTileEntity 
      (class (factory))
      (str "academy:" tile-id))))