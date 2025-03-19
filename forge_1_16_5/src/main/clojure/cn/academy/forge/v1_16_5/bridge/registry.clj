(ns cn.academy.forge.v1_16_5.bridge.registry
  (:require [cn.academy.core :as core]
            [cn.academy.forge.v1_16_5.bridge.block :as block-bridge])
  (:import [net.minecraftforge.registries ForgeRegistries IForgeRegistry]
           [net.minecraftforge.registries DeferredRegister RegistryObject]
           [net.minecraftforge.fml RegistryObject$Delegate]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent]
           [net.minecraftforge.eventbus.api IEventBus]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.item Item BlockItem]))

;; Create deferred registries for different types
(defonce blocks-registry 
  (DeferredRegister/create ForgeRegistries/BLOCKS core/modid))

(defonce items-registry 
  (DeferredRegister/create ForgeRegistries/ITEMS core/modid))

(defonce tile-entities-registry
  (DeferredRegister/create ForgeRegistries/TILE_ENTITIES core/modid))

(defonce registered-blocks (atom {}))
(defonce registered-items (atom {}))
(defonce registered-tile-entities (atom {}))

(defn register-blocks! [registry-event]
  (let [registry (.getRegistry registry-event)
        bridge (block-bridge/create-bridge)]
    (doseq [[id block] @registered-blocks]
      (.register-block bridge registry id block))))

(defn register-tile-entities! [registry-event]
  (let [registry (.getRegistry registry-event)
        bridge (block-bridge/create-bridge)]
    (doseq [[id tile] @registered-tile-entities]
      (.register-tile-entity bridge registry id tile))))

(defn register-block! [block-id block]
  (swap! registered-blocks assoc block-id block)
  block)

(defn register-tile-entity! [te-id tile-entity]
  (swap! registered-tile-entities assoc te-id tile-entity)
  tile-entity)

(defn register-item! [item-id item]
  (swap! registered-items assoc item-id item)
  item)

(defn register-deferred! []
  (let [mod-bus (.getModEventBus (FMLJavaModLoadingContext/get))]
    (.register blocks-registry mod-bus)
    (.register items-registry mod-bus)
    (.register tile-entities-registry mod-bus)))

(defn create-block-with-item [block-id item-props block-factory]
  (let [block-object (.register blocks-registry block-id
                               (reify java.util.function.Supplier
                                 (get [_]
                                   (block-factory))))
        block-item-id block-id
        item-object (.register items-registry block-item-id
                              (reify java.util.function.Supplier
                                (get [_]
                                  (BlockItem. (.get block-object) item-props))))]
    {:block block-object
     :item item-object}))

(defn initialize-registry []
  (register-deferred!))