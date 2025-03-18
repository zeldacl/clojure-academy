(ns cn.academy.forge-1-12-2.core
  (:require [cn.academy.forge-1-12-2.registration :as registration])
  (:import [net.minecraftforge.fml.common Mod Mod$EventHandler]
           [net.minecraftforge.fml.common.event FMLPreInitializationEvent FMLInitializationEvent FMLPostInitializationEvent]))

(gen-class
  :name cn.academy.forge_1_12_2.AcademyCraft
  :state state
  :init init
  :constructors {[] []}
  :prefix "mod-"
  :methods []
  :annotations [[net.minecraftforge.fml.common.Mod 
                {:modid "academy" 
                 :name "Academy Craft" 
                 :version "1.0.0"
                 :acceptedMinecraftVersions "[1.12.2]"}]])

(defn mod-init []
  (let [registration-handler (registration/create-handler)]
    (.register-deferred! registration-handler nil) ; nil since we don't use mod-bus in 1.12.2
    [[] (atom {:registration registration-handler})]))

(defn ^Mod$EventHandler mod-preInit [this ^FMLPreInitializationEvent event]
  (-> (registration/create-registration)
      (.setup-common!)))

(defn ^Mod$EventHandler mod-init [this ^FMLInitializationEvent event]
  (-> (registration/create-registration)
      (.register-all!)))

(defn ^Mod$EventHandler mod-postInit [this ^FMLPostInitializationEvent event]
  (-> (registration/create-registration)
      (.setup-client!)))