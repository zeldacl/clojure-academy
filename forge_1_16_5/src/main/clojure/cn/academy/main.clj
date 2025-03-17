(ns cn.academy.main
  (:require [cn.academy.clj-academy :as clj-academy])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.common.event FMLInitializationEvent]))

(def modid (System/getProperty "modid"))

(def name "Forge 1.16.5")
(def version "1.0")

(defn init [^FMLInitializationEvent event]
  ;; Initialize the clj_academy mode
  (clj-academy/init))

;; ...existing code...
