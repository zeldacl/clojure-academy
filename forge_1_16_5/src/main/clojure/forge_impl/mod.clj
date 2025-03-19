(ns forge-impl.mod
  (:require [forge-impl.registry-scanner :as scanner]
            [forge-impl.client-proxy :as client]
            [cn.academy.config.mod-config :as config])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.loading FMLEnvironment]
           [net.minecraftforge.api.distmarker Dist]))

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
  (let [registry (scanner/scan-and-register)]
    (swap! (.state this) assoc :registry registry)
    
    ;; Load configuration
    (config/load-config)
    
    ;; Initialize client-side components if we're on the client
    (when (= FMLEnvironment/dist Dist/CLIENT)
      (client/init))
    
    (println "ClojureAcademy mod initialized")))