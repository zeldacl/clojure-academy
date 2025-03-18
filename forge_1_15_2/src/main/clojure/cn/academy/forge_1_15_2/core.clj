(ns cn.academy.forge-1-15-2.core
  (:require [cn.academy.core.registry :as registry]
            [cn.academy.block.block.forge-cat-engine-1-15 :as cat-engine]
            [cn.academy.client.render.cat-engine-renderer :as cat-renderer])
  (:import [net.minecraftforge.fml.common.Mod]
           [net.minecraftforge.fml.event.lifecycle FMLClientSetupEvent FMLCommonSetupEvent]
           [net.minecraftforge.registries ForgeRegistries]
           [net.minecraft.block Block]
           [net.minecraft.item Item BlockItem]
           [net.minecraft.util ResourceLocation]))

(def MOD-ID "cljacademy")

(defn register-blocks []
  (let [cat-engine (cat-engine/create)
        cat-engine-block (doto (.setRegistryName (ResourceLocation. MOD-ID "cat_engine")))]
    (.register ForgeRegistries/BLOCKS cat-engine-block)
    (registry/register-block! "cat_engine" cat-engine)))

(defn register-items []
  (let [cat-engine-block (registry/get-block "cat_engine")
        cat-engine-item (doto (BlockItem. cat-engine-block (Item$Properties.))
                             (.setRegistryName (ResourceLocation. MOD-ID "cat_engine")))]
    (.register ForgeRegistries/ITEMS cat-engine-item)))

(defn register-tile-entities []
  (registry/init-registry!))

(defn setup-client [^FMLClientSetupEvent event]
  (cat-renderer/register))

(defn setup-common [^FMLCommonSetupEvent event]
  (register-blocks)
  (register-items)
  (register-tile-entities))

@Mod(MOD-ID)
(deftype CljAcademyMod []
  Object
  (constructor [this]
    (let [mod-bus (-> (FMLJavaModLoadingContext/get) .getModEventBus)]
      (.addListener mod-bus (reify Consumer
                             (accept [_ event]
                               (setup-common event))))
      (.addListener mod-bus (reify Consumer
                             (accept [_ event]
                               (setup-client event)))))))