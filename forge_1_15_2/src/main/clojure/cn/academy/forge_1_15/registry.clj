(ns cn.academy.forge-1-15.registry
  (:require [cn.academy.core.registry :as core-registry])
  (:import [net.minecraftforge.event RegistryEvent$Register]
           [net.minecraftforge.fml.client.registry ClientRegistry]
           [net.minecraft.block Block]
           [net.minecraft.tileentity TileEntityType]))

(defn create-block-instance [block-id block]
  (doto (.setRegistryName (proxy [Block] [(:properties block)]
                           (createTileEntity [state world]
             (when-let [factory (core-registry/get-tile-entity-factory block-id)]
               (factory))))
        (.setRegistryName (str "academy:" block-id))))

(defn create-tile-entity-type [tile-id factory]
  (let [supplier #(factory)]
    (-> (TileEntityType/Builder/create supplier (into-array Block []))
        .build
        (doto (.setRegistryName (str "academy:" tile-id))))))

(defn register-blocks! [^RegistryEvent$Register event]
  (let [registry (.getRegistry event)]
    (doseq [[block-id block] (:blocks @core-registry/registry-state)]
      (.register registry (create-block-instance block-id block)))))

(defn register-tile-entities! [^RegistryEvent$Register event]
  (let [registry (.getRegistry event)]
    (doseq [[tile-id factory] (:tile-entities @core-registry/registry-state)]
      (.register registry (create-tile-entity-type tile-id factory)))))