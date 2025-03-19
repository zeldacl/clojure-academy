(ns forge-impl.mod
  (:require [forge-impl.content-scanner :as scanner]
            [cn.academy.core :as acmod])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]
           [net.minecraftforge.eventbus.api IEventBus]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent]
           [net.minecraftforge.event.RegistryEvent$Register]
           [net.minecraft.block Block]
           [net.minecraft.item Item]
           [net.minecraft.tileentity TileEntityType]))

(def ^:const MOD-ID "cljacademy")

(defn- handle-setup [event]
  (println "ClojureAcademy mod setup starting...")
  ; Initialize acmod content
  (acmod/init!)
  (println "ClojureAcademy mod setup complete"))

(defn- handle-block-registry [event]
  (println "Registering blocks...")
  (scanner/register-mod-content! "acmod"))

(defn- handle-item-registry [event]
  (println "Registering items..."))  ; Items are registered with blocks

(defn- handle-tile-entity-registry [event]
  (println "Registering tile entities...")) ; TEs are registered with blocks

(gen-class
  :name forge_impl.ClojureAcademyMod
  :prefix "mod-"
  :state state
  :init init
  :constructors {[] []}
  :methods [[setup [] void]]
  :annotations {net.minecraftforge.fml.common.Mod {:value "cljacademy"}})

(defn mod-init []
  [[] (atom {})])

(defn mod-setup [this]
  (let [mod-bus (-> (FMLJavaModLoadingContext/get)
                    (.getModEventBus))]
    
    ; Register event handlers
    (.addListener mod-bus (reify java.util.function.Consumer
                           (accept [_ e]
                             (when (instance? FMLCommonSetupEvent e)
                               (handle-setup e)))))
    
    (.addGenericListener mod-bus Block
                        (reify java.util.function.Consumer
                          (accept [_ e]
                            (when (instance? RegistryEvent$Register e)
                              (handle-block-registry e)))))
    
    (.addGenericListener mod-bus Item
                        (reify java.util.function.Consumer
                          (accept [_ e]
                            (when (instance? RegistryEvent$Register e)
                              (handle-item-registry e)))))
    
    (.addGenericListener mod-bus TileEntityType
                        (reify java.util.function.Consumer
                          (accept [_ e]
                            (when (instance? RegistryEvent$Register e)
                              (handle-tile-entity-registry e)))))
    
    (println "ClojureAcademy mod initialized")))