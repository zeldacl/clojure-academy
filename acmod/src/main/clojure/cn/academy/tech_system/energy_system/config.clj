(ns cn.academy.tech-system.energy-system.config
  (:require [mcmod.config :as config]
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

(def config-state (atom defaults))

(defn get-config
  "Get a configuration value by path. Path can be a sequence of keys."
  [& path]
  (get-in @config-state path))

(defn load-config!
  "Load configuration from file, merging with defaults"
  []
  (try
    (let [file-config (config/load-config "energy_system.conf")]
      (reset! config-state (merge-with merge defaults file-config))
      (log/info "Energy system configuration loaded successfully"))
    (catch Exception e
      (log/error e "Failed to load energy system configuration - using defaults"))))

(defn save-config!
  "Save current configuration to file"
  []
  (try
    (config/save-config! "energy_system.conf" @config-state)
    (log/info "Energy system configuration saved successfully")
    (catch Exception e
      (log/error e "Failed to save energy system configuration"))))