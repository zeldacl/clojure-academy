(ns forge-impl.core
  (:require [forge-impl.registry-scanner :as scanner]
            [forge-impl.dependency-scanner :as deps]
            [forge-impl.content-scanner :as content]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]
           [net.minecraftforge.eventbus.api IEventBus SubscribeEvent]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent]
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

(gen-class
  :name forge_impl.ForgeInitializer
  :prefix "forge-"
  :methods [[init [] void]
            [^{SubscribeEvent true} onCommonSetup [net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent] void]]
  :annotations [[net.minecraftforge.fml.common.Mod "forge_1_16_5"]])

(defn forge-init []
  (log/info "Initializing Forge 1.16.5 implementation"))

(defn forge-onCommonSetup [this event]
  (log/info "Starting content registration")
  (scanner/scan-and-register!)
  (log/info "Content registration complete"))