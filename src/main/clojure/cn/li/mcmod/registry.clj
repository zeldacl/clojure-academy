(ns cn.li.mcmod.registry
  (:require [clojure.tools.logging :as log]
            [cn.li.mcmod.utils :refer [get-fullname construct client?]]
    ;[cn.li.mcmod.blocks :refer [instance-block]]
            [cn.li.mcmod.global :refer [*mod-id*]]
    ;[cn.li.academy.energy.blocks.node :refer [block-node]]
            )
  (:import (net.minecraftforge.fml.common Mod$EventBusSubscriber Mod$EventBusSubscriber$Bus)
           (net.minecraftforge.eventbus.api SubscribeEvent EventPriority)
           (net.minecraftforge.event RegistryEvent$Register)
           (net.minecraft.block Block)
           (net.minecraft.item ItemGroup ItemStack Item Item$Properties BlockItem)
           (net.minecraft.util ResourceLocation)
           (net.minecraft.tileentity TileEntityType TileEntityType$Builder)
           (java.util.function Supplier)
           (net.minecraftforge.registries IForgeRegistryEntry)))

(defonce ^:dynamic *item-group* nil)


(defn set-registry-name [^IForgeRegistryEntry block-instance ^String registry-name]
  ;(log/info "*******************  " *mod-id* registry-name)
  (let [registry-name (if (instance? ResourceLocation registry-name)
                        registry-name
                        (ResourceLocation. *mod-id* registry-name))]
    (.setRegistryName block-instance registry-name)))

(defonce ^:dynamic *registry-items* (atom []))
(defn registry-item [item]
  (swap! *registry-items* conj item))

(defn on-items-registry [^RegistryEvent$Register event]
  (log/info "qqqqqqqqqqqqqqqqqqq4444  " *registry-items*)
  (dorun (map #(.register (.getRegistry event) %1) (deref *registry-items*)))

  )

(defonce ^:dynamic *registry-blocks* (atom []))

(defn registry-block
  ([^Block block]
   (registry-block block false nil))
  ([^Block block registry-item?]
   (registry-block block registry-item? nil))
  ([^Block block registry-item? ^ResourceLocation registry-name]
   (swap! *registry-blocks* conj block)
   (when registry-item?
     (let [properties (.group (Item$Properties.) *item-group*)
           item (BlockItem. block properties)
           ^ResourceLocation registry-name (or registry-name (.getRegistryName block))  ]
       (registry-item (.setRegistryName item registry-name))))))

(defn on-blocks-registry [^RegistryEvent$Register event]
  (log/info "qqqqqqqqqqqqqqqqqqq333  " *registry-blocks*)
  ;(let [ss ^Block (instance-block block-node)]
  ;  (.setRegistryName ss "basenode")
  ;  (.register (.getRegistry event) ss))
  (dorun (map #(.register (.getRegistry event) %1) (deref *registry-blocks*)))
  )

(defonce ^:dynamic *registry-tile-entity* (atom []))

(defn registry-tile-entity [tile-entity block-instance ^ResourceLocation registry-name]
  (let [builder (TileEntityType$Builder/create (proxy [Supplier] []
                                                          (get []
                                                            (construct tile-entity)))
                  (into-array [block-instance]))
        ^TileEntityType tile-entity-type (.build builder nil)]
    (swap! *registry-tile-entity* conj (.setRegistryName tile-entity-type registry-name))))

(defn on-tile-entity-registry [^RegistryEvent$Register event]
  (log/info "qqqqqqqqqqqqqqqqqqq666  " *registry-tile-entity*)
  ;(let [ss ^Block (instance-block block-node)]
  ;  (.setRegistryName ss "basenode")
  ;  (.register (.getRegistry event) ss))
  (dorun (map #(.register (.getRegistry event) %1) (deref *registry-tile-entity*)))
  )

(defonce ^:dynamic *registry-block-container* (atom []))

(defn registry-block-container [container-type]
  (swap! *registry-block-container* conj container-type))

(defn on-blocks-container-registry [^RegistryEvent$Register event]
  (log/info "qqqqqqqqqqqqqqqqqqq555  " *registry-block-container*)
  ;(let [ss ^Block (instance-block block-node)]
  ;  (.setRegistryName ss "basenode")
  ;  (.register (.getRegistry event) ss))
  (dorun (map #(.register (.getRegistry event) %1) (deref *registry-block-container*)))
  )


(defn registry-block-struct [block-struct]
  (let [^Block block (:block-instance block-struct)
        ^ResourceLocation registry-name (or (:registry-name block-struct) (.getRegistryName block))]
    (registry-block block true registry-name)
    (when-let [tile-entity (:tile-entity block-struct)]
      (registry-tile-entity tile-entity block registry-name))
    (when-let [container-type (:container-type block-struct)]
      (registry-block-container container-type))
    ))

