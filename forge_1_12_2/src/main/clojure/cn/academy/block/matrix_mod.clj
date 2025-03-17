(ns cn.academy.block.matrix-mod
  (:require [cn.academy.block.matrix-init :as init]
            [cn.academy.block.matrix-forge-registry :as registry])
  (:import [net.minecraftforge.fml.common Mod Mod$EventHandler]
           [net.minecraftforge.fml.common.event FMLPreInitializationEvent 
                                               FMLInitializationEvent
                                               FMLPostInitializationEvent]))

(def MOD-ID "academy")

(defonce matrix-registry (atom nil))

(defrecord MatrixMod []
  Object
  (^Mod$EventHandler preInit [_ ^FMLPreInitializationEvent event]
    (let [initializer (init/create-initializer MOD-ID)
          registry (registry/register-matrix-preinit MOD-ID)]
      (reset! matrix-registry registry)))
  
  (^Mod$EventHandler init [_ ^FMLInitializationEvent event]
    (when-let [registry @matrix-registry]
      (registry/register-matrix-init registry)))
  
  (^Mod$EventHandler postInit [_ ^FMLPostInitializationEvent event]
    (when-let [registry @matrix-registry]
      (when (.isClient event)
        (registry/register-matrix-client-init registry)))))

(defn create-mod []
  (->MatrixMod))

(Mod :modid MOD-ID
     :name "Academy Craft"
     :version "1.0.0")
(def INSTANCE (create-mod))