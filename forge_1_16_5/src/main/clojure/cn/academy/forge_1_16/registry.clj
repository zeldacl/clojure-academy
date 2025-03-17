(ns cn.academy.forge-1-16.registry
  (:require [cn.academy.core.registry :as core-registry])
  (:import [net.minecraftforge.event RegistryEvent$Register]
           [net.minecraftforge.fml.client.registry ClientRegistry]
           [net.minecraft.block Block]
           [net.minecraft.tileentity TileEntityType]
           [net.minecraft.util ResourceLocation]))

(defn create-block-instance [block-id block]
  (doto (.setRegistryName (proxy [Block] [(:properties block)]
                           (createTileEntity [state world]
             (when-let [factory (core-registry/get-tile-entity-factory block-id)]
               (factory))))
        (.setRegistryName (ResourceLocation. "academy" block-id))))

(defn create-tile-entity-type [tile-id factory]
  (let [supplier #(factory)]
    (-> (TileEntityType/Builder/of supplier (into-array Block []))  ; Changed from create to of in 1.16
        .build
        (doto (.setRegistryName (ResourceLocation. "academy" tile-id))))))

(defn register-blocks! [^RegistryEvent$Register event]
  (let [registry (.getRegistry event)]
    (doseq [[block-id block] (:blocks @core-registry/registry-state)]
      (.register registry (create-block-instance block-id block)))))

(defn register-tile-entities! [^RegistryEvent$Register event]
  (let [registry (.getRegistry event)]
    (doseq [[tile-id factory] (:tile-entities @core-registry/registry-state)]
      (.register registry (create-tile-entity-type tile-id factory)))))