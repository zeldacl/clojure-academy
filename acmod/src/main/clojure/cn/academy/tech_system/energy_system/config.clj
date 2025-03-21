(ns cn.academy.tech-system.energy-system.config
  (:require [cn.academy.core.config :as core-config]
            [clojure.edn :as edn]
            [mcmod.config :as config]
            [clojure.tools.logging :as log]))

;; Default configuration values
(def ^:private defaults
  {:wireless-range 16
   :allow-interdimensional false
   :energy-transfer-rate 1000
   :node-types 
   {:basic {:max-energy 5000
            :range 8
            :max-connections 4
            :bandwidth 500}
    :standard {:max-energy 20000
               :range 16
               :max-connections 8
               :bandwidth 1000}
    :advanced {:max-energy 100000
               :range 32
               :max-connections 16
               :bandwidth 2000}}
   :security
   {:max-failed-attempts 5
    :ban-duration 3600
    :require-password true}
   :visualization
   {:particle-density 1.0
    :show-connections true
    :connection-opacity 0.7}})

(def ^:private default-config
  {:network
   {:max-batch-size 32768
    :batch-threshold 10
    :batch-interval 50
    :max-connections 16
    :max-range 32
    :connection-timeout 30000}
   
   :security
   {:max-attempts 5
    :block-duration 300000
    :password-min-length 6}
   
   :optimization
   {:bandwidth-window 60000
    :balance-threshold 0.2
    :compression-threshold 1024}
   
   :storage
   {:save-interval 300000
    :backup-count 3}})

(def config-state
  (atom default-config))

(defn load-config! []
  (try
    (when-let [config (core-config/get-config [:tech-system :energy])]
      (swap! config-state merge config))
    (catch Exception e
      (println "Error loading energy system config:" (.getMessage e)))))

(defn get-config
  ([path]
   (get-in @config-state path))
  ([path default]
   (get-in @config-state path default)))

(defn set-config! [path value]
  (swap! config-state assoc-in path value)
  (core-config/set-config! [:tech-system :energy] @config-state))

(defn reset-config! []
  (reset! config-state default-config)
  (core-config/set-config! [:tech-system :energy] default-config))

(defn validate-config [config]
  (and
    ;; Network settings
    (>= (get-in config [:network :max-batch-size] 0) 1024)
    (>= (get-in config [:network :batch-threshold] 0) 1)
    (>= (get-in config [:network :batch-interval] 0) 20)
    (>= (get-in config [:network :max-connections] 0) 1)
    (>= (get-in config [:network :max-range] 0) 1)
    
    ;; Security settings
    (>= (get-in config [:security :max-attempts] 0) 1)
    (>= (get-in config [:security :block-duration] 0) 1000)
    (>= (get-in config [:security :password-min-length] 0) 4)
    
    ;; Optimization settings
    (>= (get-in config [:optimization :bandwidth-window] 0) 1000)
    (>= (get-in config [:optimization :balance-threshold] 0) 0)
    (<= (get-in config [:optimization :balance-threshold] 1) 1)
    
    ;; Storage settings
    (>= (get-in config [:storage :save-interval] 0) 1000)
    (>= (get-in config [:storage :backup-count] 0) 1)))