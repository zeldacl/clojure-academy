(ns forge-impl.core
  (:require [forge-impl.registry-scanner :as scanner]
            [forge-impl.dependency-scanner :as deps]
            [forge-impl.content-scanner :as content])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]
           [net.minecraftforge.eventbus.api IEventBus]
           [net.minecraft.item ItemGroup ItemStack]
           [net.minecraft.item Items]))

(def MODID "cljacademy")

(def creative-tab
  (proxy [ItemGroup] ["cljacademy"]
    (makeIcon []
      (ItemStack. Items/DIAMOND))))

(defn register-blocks [registry-event]
  (deps/scan-dependencies!)
  (content/register-scanned-content!))

(defn register-items [registry-event]
  (deps/scan-dependencies!)
  (content/register-scanned-content!))

(defn register-tile-entities [registry-event]
  (deps/scan-dependencies!)
  (content/register-scanned-content!))

(gen-class
 :name forge_impl.ForgeModInitializer
 :prefix "mod-"
 :state state
 :init init
 :constructors {[] []}
 :methods []
 :annotations [[net.minecraftforge.fml.common.Mod "mcmod"]])

(defn mod-init []
  [[] (atom {})])

(defn mod-setup [this]
  (let [mod-bus (-> (FMLJavaModLoadingContext/get)
                    (.getModEventBus))]
    (.addListener mod-bus register-blocks)
    (.addListener mod-bus register-items)
    (.addListener mod-bus register-tile-entities)))