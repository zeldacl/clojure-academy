(ns cn.academy.block.matrix-mod
  (:require [cn.academy.block.matrix-init :as init]
            [cn.academy.block.matrix-forge-registry :as registry])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent 
                                                  FMLClientSetupEvent]
           [net.minecraftforge.eventbus.api IEventBus 
                                          SubscribeEvent]))

(def MOD-ID "academy")

(defonce matrix-registry (atom nil))

(defrecord MatrixMod []
  Object
  (^void onCommonSetup [_ ^FMLCommonSetupEvent event]
    (let [initializer (init/create-initializer MOD-ID)
          registry (registry/register-matrix MOD-ID)]
      (reset! matrix-registry registry)))
  
  (^void onClientSetup [_ ^FMLClientSetupEvent event]
    (when-let [registry @matrix-registry]
      (.register-renderer registry event))))

(defn create-mod []
  (let [mod (->MatrixMod)
        event-bus (-> (FMLJavaModLoadingContext/get)
                     .getModEventBus)]
    (.addListener event-bus #(.onCommonSetup mod %))
    (.addListener event-bus #(.onClientSetup mod %))
    mod))

(Mod MOD-ID)
(def INSTANCE (create-mod))