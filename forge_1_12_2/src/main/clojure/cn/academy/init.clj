(ns cn.academy.init
  (:require [cn.academy.core.util.logging :refer [log-info log-debug]]
            [cn.academy.core.util.diagnostics :as diag]
            [cn.academy.core.util.monitoring :as mon]
            [cn.academy.core.util.profiling :as prof])
  (:import [net.minecraftforge.fml.common Mod Mod$EventHandler]
           [net.minecraftforge.fml.common.event FMLPreInitializationEvent 
                                              FMLInitializationEvent
                                              FMLPostInitializationEvent]))

(defn init-monitoring! []
  (mon/set-threshold! :loading 1000) ; Alert on loading operations over 1 second
  (mon/set-threshold! :tick 50))     ; Alert on tick operations over 50ms

(defn write-startup-diagnostics! [event]
  (let [config-dir (.getConfigurationDir (.getModConfigurationDirectory event))]
    (diag/write-diagnostic-report! 
      (io/file config-dir "academy-startup-diagnostics.txt"))))

(defn pre-init [event]
  (prof/profile :pre-init
    (try
      (log-info "Academy pre-initialization starting...")
      (init-monitoring!)
      (write-startup-diagnostics! event)
      (catch Throwable t
        (log-debug t "Error during pre-initialization")
        (throw t)))))

(defn init [event]
  (prof/profile :init
    (try 
      (log-info "Academy initialization starting...")
      (catch Throwable t
        (log-debug t "Error during initialization")
        (throw t)))))

(defn post-init [event]
  (prof/profile :post-init
    (try
      (log-info "Academy post-initialization starting...")
      (catch Throwable t
        (log-debug t "Error during post-initialization")
        (throw t)))))