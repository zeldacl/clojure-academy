(ns cn.li.mcmod.registry
  (:require [clojure.tools.logging :as log]
            [cn.li.mcmod.utils :refer [get-fullname]]
    ;[cn.li.mcmod.blocks :refer [instance-block]]
            [cn.li.mcmod.global :refer [*mod-id*]]
    ;[cn.li.academy.energy.blocks.node :refer [block-node]]
            )
  (:import (net.minecraftforge.fml.common Mod$EventBusSubscriber Mod$EventBusSubscriber$Bus)
           (net.minecraftforge.eventbus.api SubscribeEvent EventPriority)
           (net.minecraftforge.event RegistryEvent$Register)
           (net.minecraft.block Block)
           (net.minecraft.item ItemGroup ItemStack Item Item$Properties BlockItem)
           (net.minecraft.util ResourceLocation)))

(defonce ^:dynamic *item-group* nil)
(defn create-item-group [label icon]
  (proxy [ItemGroup] [label]
    (createIcon []
      (ItemStack. icon))))

(defn init-item-group [label icon]
  (alter-var-root #'*item-group* (constantly (create-item-group label icon))))


(defn set-registry-name [^Block block-instance ^String registry-name]
  (log/info "*******************  " *mod-id* registry-name)
  (.setRegistryName block-instance  *mod-id* registry-name))

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
   (registry-block block false))
  ([^Block block registry-item?]
   (swap! *registry-blocks* conj block)
   (when registry-item?
     (let [properties (.group (Item$Properties.) *item-group*)
           item (BlockItem. block properties)
           registry-name ^ResourceLocation (.getRegistryName block)]
       (registry-item (.setRegistryName item registry-name))))))

(defn on-blocks-registry [^RegistryEvent$Register event]
  (log/info "qqqqqqqqqqqqqqqqqqq333  " *registry-blocks*)
  ;(let [ss ^Block (instance-block block-node)]
  ;  (.setRegistryName ss "basenode")
  ;  (.register (.getRegistry event) ss))
  (dorun (map #(.register (.getRegistry event) %1) (deref *registry-blocks*)))
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


