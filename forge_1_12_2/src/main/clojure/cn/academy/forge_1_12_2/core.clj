(ns cn.academy.forge-1-12-2.core
  (:require [cn.academy.core.registry :as registry]
            [cn.academy.block.block.forge-cat-engine-1-12 :as cat-engine]
            [cn.academy.client.render.cat-engine-renderer :as cat-renderer])
  (:import [net.minecraftforge.fml.common Mod Mod$EventHandler]
           [net.minecraftforge.fml.common.event FMLPreInitializationEvent FMLInitializationEvent]
           [net.minecraftforge.event RegistryEvent$Register]
           [net.minecraft.block Block]
           [net.minecraft.item Item ItemBlock]
           [net.minecraft.util ResourceLocation]))

(def MOD-ID "cljacademy")

(defn register-blocks [^RegistryEvent$Register event]
  (let [cat-engine (cat-engine/create)
        cat-engine-block (doto (.setRegistryName (ResourceLocation. MOD-ID "cat_engine"))
                              (.setUnlocalizedName "cat_engine"))]
    (.register (.getRegistry event) cat-engine-block)
    (registry/register-block! "cat_engine" cat-engine)))

(defn register-items [^RegistryEvent$Register event]
  (let [cat-engine-block (registry/get-block "cat_engine")
        cat-engine-item (doto (ItemBlock. cat-engine-block)
                             (.setRegistryName (ResourceLocation. MOD-ID "cat_engine")))]
    (.register (.getRegistry event) cat-engine-item)))

(defn register-tile-entities []
  (registry/init-registry!))

@Mod(modid = MOD-ID)
(deftype CljAcademyMod []
  Object
  @Mod$EventHandler
  (preInit [this event]
    (register-tile-entities))
  
  @Mod$EventHandler
  (init [this event]
    (when (.isClient (FMLCommonHandler/instance))
      (cat-renderer/register))))