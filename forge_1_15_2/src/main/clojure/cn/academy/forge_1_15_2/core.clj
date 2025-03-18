(ns cn.academy.forge-1-15-2.core
  (:require [cn.academy.forge-1-15-2.registration :as registration])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent FMLClientSetupEvent]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]
           [java.util.function Consumer]))

(defn setup-common [^FMLCommonSetupEvent event]
  (-> (registration/create-registration)
      (.setup-common!)))

(defn setup-client [^FMLClientSetupEvent event]
  (-> (registration/create-registration)
      (.setup-client!)))

(gen-class
  :name cn.academy.forge_1_15_2.AcademyCraft
  :state state
  :init init
  :constructors {[] []}
  :prefix "mod-"
  :methods [[setup [net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent] void]
            [setupClient [net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent] void]]
  :annotations [[net.minecraftforge.fml.common.Mod "academy"]])

(defn mod-init []
  (let [mod-bus (-> (FMLJavaModLoadingContext/get)
                    .getModEventBus)
        registration-handler (registration/create-handler)]
    (.addListener mod-bus 
                 (reify Consumer
                   (accept [_ event]
                     (setup-common event))))
    (.addListener mod-bus
                 (reify Consumer
                   (accept [_ event]
                     (setup-client event))))
    ;; Register content
    (.register-deferred! registration-handler mod-bus)
    (.register-all! (registration/create-registration)))
  [[] (atom {})])