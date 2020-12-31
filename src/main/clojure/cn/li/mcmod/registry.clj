(ns cn.li.mcmod.registry
  (:require [clojure.tools.logging :as log]
            [cn.li.mcmod.utils :refer [get-fullname construct client? ->Supplier]]
    ;[cn.li.mcmod.blocks :refer [instance-block]]
            [cn.li.mcmod.global :refer [*mod-id* *blocks* *tile-entities* *block-items* *containers* *item-group*]]
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
           (net.minecraftforge.registries IForgeRegistryEntry DeferredRegister ForgeRegistries)
           (net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext)
           (net.minecraftforge.common.extensions IForgeContainerType)
           (net.minecraftforge.fml.network IContainerFactory)
           (net.minecraft.network PacketBuffer)
           (net.minecraft.entity.player PlayerInventory)
           (net.minecraft.util.math BlockPos)
           (net.minecraft.world World)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;(def deferred-registers
;  {
;   :blocks        {
;                   :type ForgeRegistries/BLOCKS
;                   }
;   :items         {
;                   :type ForgeRegistries/ITEMS
;                   }
;   :tile_entities {
;                   :type ForgeRegistries/TILE_ENTITIES
;                   }
;   :containers    {
;                   :type ForgeRegistries/CONTAINERS
;                   }
;   :entities      {
;                   :type ForgeRegistries/ENTITIES
;                   }
;   :dimensions    {
;                   :type ForgeRegistries/MOD_DIMENSIONS
;                   }
;   })

;(doseq [name (keys deferred-registers)]
;  (println "vcxvxv6734563463463  " name))
;
;(def deferred-names ["blocks" "items" "tiles" "containers" "entities" "dimensions"])
;(defn- get-deferred-name [name]
;  (str "*deferred-register-" name "*"))
;;(defmacro def-deferred-register [names]
;;  `(doseq [name ~names]
;;    (def ^:dynamic ~(symbol (get-deferred-name name)) nil)))
;;(def-deferred-register deferred-names)
;(doseq [name (keys deferred-registers)]
;  (def ^:dynamic (symbol (get-deferred-name name)) nil))


(def ^:dynamic *deferred-registers*
  (atom {
         :blocks        {
                         :type ForgeRegistries/BLOCKS
                         :registers         {}
                         }
         :items         {
                         :type ForgeRegistries/ITEMS
                         :registers         {}
                         }
         :tile_entities {
                         :type ForgeRegistries/TILE_ENTITIES
                         :registers         {}
                         }
         :containers    {
                         :type ForgeRegistries/CONTAINERS
                         :registers         {}
                         }
         :entities      {
                         :type ForgeRegistries/ENTITIES
                         :registers         {}
                         }
         :dimensions    {
                         :type ForgeRegistries/MOD_DIMENSIONS
                         :registers         {}
                         }
         }))

;(def ^:dynamic *deferred-register-blocks* nil)
;(def ^:dynamic *deferred-register-items* nil)
;(def ^:dynamic *deferred-register-tiles* nil)
;(def ^:dynamic *deferred-register-containers* nil)
;(def ^:dynamic *deferred-register-entities* nil)
;(def ^:dynamic *deferred-register-dimensions* nil)


(defn init-deferred-register [^String mod-id]
  (doseq [name (keys @*deferred-registers*)]
    (let [register (DeferredRegister. (get-in @*deferred-registers* [name :type]) mod-id)]
      (swap! *deferred-registers* update-in [name :deferred-register] (constantly register))))
  ;(alter-var-root #'*deferred-register-blocks* (constantly (DeferredRegister. ForgeRegistries/BLOCKS mod-id)))
  ;(alter-var-root #'*deferred-register-items* (constantly (DeferredRegister. ForgeRegistries/ITEMS mod-id)))
  ;(alter-var-root #'*deferred-register-tiles* (constantly (DeferredRegister. ForgeRegistries/TILE_ENTITIES mod-id)))
  ;(alter-var-root #'*deferred-register-containers* (constantly (DeferredRegister. ForgeRegistries/CONTAINERS mod-id)))
  ;(alter-var-root #'*deferred-register-entities* (constantly (DeferredRegister. ForgeRegistries/ENTITIES mod-id)))
  ;(alter-var-root #'*deferred-register-dimensions* (constantly (DeferredRegister. ForgeRegistries/MOD_DIMENSIONS mod-id)))
  )

(defn register-event []
  (doseq [register (vals @*deferred-registers*)]
    (.register ^DeferredRegister (:deferred-register register) (.getModEventBus (FMLJavaModLoadingContext/get))))
  ;(.register ^DeferredRegister *deferred-register-blocks* (.getModEventBus (FMLJavaModLoadingContext/get)))
  ;(.register ^DeferredRegister *deferred-register-items* (.getModEventBus (FMLJavaModLoadingContext/get)))
  ;(.register ^DeferredRegister *deferred-register-tiles* (.getModEventBus (FMLJavaModLoadingContext/get)))
  ;(.register ^DeferredRegister *deferred-register-containers* (.getModEventBus (FMLJavaModLoadingContext/get)))
  ;(.register ^DeferredRegister *deferred-register-entities* (.getModEventBus (FMLJavaModLoadingContext/get)))
  ;(.register ^DeferredRegister *deferred-register-dimensions* (.getModEventBus (FMLJavaModLoadingContext/get)))
  )

(defn get-instance [type registry-name]
  (get-in @*deferred-registers* [type :registers registry-name]))

(defn register-to-deferred [deferred-register name fn]
  (.register
    ^DeferredRegister deferred-register
    name
    (->Supplier fn)))

(defn register-obj [type registry-name instance-fn]
  (swap! *deferred-registers*
    (fn [deferred-registers]
      (let [block-instance (register-to-deferred (get-in deferred-registers [type :deferred-register]) registry-name instance-fn)]
        (assoc-in deferred-registers [type :registers registry-name] block-instance)))))

(defn register-block [registry-name instance-fn]
  (register-obj :blocks registry-name instance-fn))

(defn register-item [registry-name instance-fn]
  (register-obj :items registry-name instance-fn))

(defn register-tileentity [registry-name instance-fn]
  (register-obj :tile_entities registry-name instance-fn))

(defn register-container [registry-name instance-fn]
  (register-obj :containers registry-name instance-fn))

(defn register-block-item [registry-name]
  (let [block-instance (get-instance :blocks registry-name)
        instance-fn (fn []
                      (BlockItem.
                        (.get block-instance)
                        (.group (Item$Properties.) *item-group*)))]
    (register-item registry-name instance-fn)))

(defn registry-block-struct [block-struct]
  (let [instance-fn (:instance-fn block-struct)
        registry-name (:registry-name block-struct)
        ;block-instance (register-obj *deferred-register-blocks* registry-name instance-fn)
        ;^ResourceLocation registry-name (or (:registry-name block-struct) (.getRegistryName block))
        ]
    ;(swap! *blocks* assoc registry-name block-instance)
    ;(registry-block block true registry-name)
    (register-block registry-name instance-fn)

    ;; block-items
    (when-let [registry-item? (:registry-item? block-struct)]
      (register-block-item registry-name)
      ;(let [properties (.group (Item$Properties.) *item-group*)
      ;      item (BlockItem. (.get block-instance) properties)]
      ;  (swap! *block-items* assoc registry-name item))
      )
    ;TileEntityType.Builder.create(FancyBlockTile::new, FANCYBLOCK.get()).build(null)

    ;; tile-entity
    (when-let [tile-entity (:tile-entity block-struct)]
      (let [tile-entity-create-fn (:tile-entity-create-fn block-struct)
            block-instance (get-instance :blocks registry-name)
            entity-instance-fn (fn []
                                 (.build (TileEntityType$Builder/create
                                           (->Supplier tile-entity-create-fn)
                                           (.get block-instance)) nil))
            ;entity-instance (register-obj *deferred-register-tiles* registry-name entity-instance-fn)
            ]
        (register-tileentity registry-name entity-instance-fn)
        ;(swap! *tile-entities* assoc registry-name entity-instance)
        ))

    ;; container
    (when-let [container (:container block-struct)]
      (let [container-create-fn (:container-create-fn block-struct)
            container-instance-fn (fn []
                                    (IForgeContainerType/create (proxy [IContainerFactory] []
                                                                  (create [window-id ^PlayerInventory inv ^PacketBuffer data]
                                                                    (let [^BlockPos pos (.readBlockPos data)
                                                                          ^World world (.getEntityWorld (.-player inv))
                                                                          container-type (get-instance :containers registry-name)]
                                                                      (container-create-fn container-type window-id world pos inv (.-player inv)))))))
            ;container-instance (register-obj *deferred-register-containers* registry-name container-instance-fn)
            ]
        (register-container registry-name container-instance-fn)
        ;(swap! *containers* assoc registry-name container-instance)
        ))
    ;(when-let [container-type (:container-type block-struct)]
    ;  (registry-block-container container-type))
    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;(defonce ^:dynamic *item-group* nil)
;
;
;(defn set-registry-name [^IForgeRegistryEntry block-instance ^String registry-name]
;  ;(log/info "*******************  " *mod-id* registry-name)
;  (let [registry-name (if (instance? ResourceLocation registry-name)
;                        registry-name
;                        (ResourceLocation. *mod-id* registry-name))]
;    (.setRegistryName block-instance registry-name)))
;
;(defonce ^:dynamic *registry-items* (atom []))
;(defn registry-item [item]
;  (swap! *registry-items* conj item))
;
;(defn on-items-registry [^RegistryEvent$Register event]
;  (log/info "qqqqqqqqqqqqqqqqqqq4444  " *registry-items*)
;  (dorun (map #(.register (.getRegistry event) %1) (deref *registry-items*)))
;
;  )
;
;(defonce ^:dynamic *registry-blocks* (atom []))
;
;(defn registry-block
;  ([^Block block]
;   (registry-block block false nil))
;  ([^Block block registry-item?]
;   (registry-block block registry-item? nil))
;  ([^Block block registry-item? ^ResourceLocation registry-name]
;   (swap! *registry-blocks* conj block)
;   (when registry-item?
;     (let [properties (.group (Item$Properties.) *item-group*)
;           item (BlockItem. block properties)
;           ^ResourceLocation registry-name (or registry-name (.getRegistryName block))  ]
;       (registry-item (.setRegistryName item registry-name))))))
;
;(defn on-blocks-registry [^RegistryEvent$Register event]
;  (log/info "qqqqqqqqqqqqqqqqqqq333  " *registry-blocks*)
;  ;(let [ss ^Block (instance-block block-node)]
;  ;  (.setRegistryName ss "basenode")
;  ;  (.register (.getRegistry event) ss))
;  (dorun (map #(.register (.getRegistry event) %1) (deref *registry-blocks*)))
;  )
;
;(defonce ^:dynamic *registry-tile-entity* (atom []))
;
;(defn registry-tile-entity [tile-entity block-instance ^ResourceLocation registry-name]
;  (let [builder (TileEntityType$Builder/create (proxy [Supplier] []
;                                                          (get []
;                                                            (construct tile-entity)))
;                  (into-array [block-instance]))
;        ^TileEntityType tile-entity-type (.build builder nil)]
;    (swap! *registry-tile-entity* conj (.setRegistryName tile-entity-type registry-name))))
;
;(defn on-tile-entity-registry [^RegistryEvent$Register event]
;  (log/info "qqqqqqqqqqqqqqqqqqq666  " *registry-tile-entity*)
;  ;(let [ss ^Block (instance-block block-node)]
;  ;  (.setRegistryName ss "basenode")
;  ;  (.register (.getRegistry event) ss))
;  (dorun (map #(.register (.getRegistry event) %1) (deref *registry-tile-entity*)))
;  )
;
;(defonce ^:dynamic *registry-block-container* (atom []))
;
;(defn registry-block-container [container-type]
;  (swap! *registry-block-container* conj container-type))
;
;(defn on-blocks-container-registry [^RegistryEvent$Register event]
;  (log/info "qqqqqqqqqqqqqqqqqqq555  " *registry-block-container*)
;  ;(let [ss ^Block (instance-block block-node)]
;  ;  (.setRegistryName ss "basenode")
;  ;  (.register (.getRegistry event) ss))
;  (dorun (map #(.register (.getRegistry event) %1) (deref *registry-block-container*)))
;  )
;
;
;(defn registry-block-struct [block-struct]
;  (let [^Block block (:block-instance block-struct)
;        ^ResourceLocation registry-name (or (:registry-name block-struct) (.getRegistryName block))]
;    (registry-block block true registry-name)
;    (when-let [tile-entity (:tile-entity block-struct)]
;      (registry-tile-entity tile-entity block registry-name))
;    (when-let [container-type (:container-type block-struct)]
;      (registry-block-container container-type))
;    ))

