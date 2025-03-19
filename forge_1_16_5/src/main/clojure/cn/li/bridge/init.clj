(ns cn.li.bridge.init
  (:require [cn.li.bridge.registry.api :as registry]
            [cn.li.bridge.matrix.init :as matrix]
            [cn.li.bridge.matrix.render :as render]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent FMLClientSetupEvent]
           [net.minecraftforge.eventbus.api SubscribeEvent]
           [net.minecraftforge.api.distmarker Dist OnlyIn]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]))

(def MOD_ID "li")

(defn initialize-common []
  (log/info "Initializing common components...")
  (let [registry-handler (registry/get-registry-handler)
        matrix-components (matrix/register-matrix registry-handler)]
    (log/info "Matrix system registered successfully")))

(defn initialize-client []
  (log/info "Initializing client components...")
  (let [registry-handler (registry/get-registry-handler)]
    (render/register-renderer registry-handler)))

(defrecord ModInitializer []
  Object
  (init [_]
    (let [event-bus (.getEventBus (FMLJavaModLoadingContext/get))]
      (.addListener event-bus #(initialize-common))
      (.addListener event-bus #(initialize-client))))
  
  (^SubscribeEvent onCommonSetup [_ ^FMLCommonSetupEvent event]
    (.enqueueWork event #(initialize-common)))
  
  (^SubscribeEvent ^OnlyIn onClientSetup [_ ^FMLClientSetupEvent event]
    (.enqueueWork event #(initialize-client))))

(defn create-mod []
  (->ModInitializer))

(def ^:const INSTANCE
  (create-mod))

(Mod :modid MOD_ID
     :value "Li Bridge Mod")