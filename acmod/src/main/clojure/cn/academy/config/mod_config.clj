(ns cn.academy.config.mod-config
  (:require [mcmod.config :as config])
  (:import [net.minecraftforge.fml.config ModConfig$Type ConfigBuilder]))

(def default-config
  {:wireless-matrix
   {:energy-capacity 100000
    :max-transfer-rate 1000
    :particle-count 5
    :sound-volume 0.15
    :base-pitch 0.8}})

(defn load-config []
  (config/set-config! :wireless-matrix
                     (get (config/get-config) 
                          :wireless-matrix 
                          (:wireless-matrix default-config))))

(defn get-wireless-matrix-config []
  (get (config/get-config) :wireless-matrix))