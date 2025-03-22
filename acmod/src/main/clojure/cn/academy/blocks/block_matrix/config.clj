(ns cn.academy.blocks.block-matrix.config
  "Configuration for the wireless matrix block.
   Contains default settings and configuration loading/saving logic."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [cn.academy.blocks.block-matrix.utils :as utils]))

;; Default configuration map
(def default-config
  {:energy {:base-capacity 100000
            :max-capacity 1000000
            :core-multiplier 2.0
            :base-transfer-rate 1000
            :transfer-multiplier 1.5
            :base-consumption 10
            :activation-cost 100}
   
   :network {:base-range 16.0
             :range-multiplier 1.5
             :max-nodes 64
             :scan-interval-ticks 20
             :broadcast-interval-ticks 100
             :discovery-range 32.0
             :discovery-energy-cost 500}
   
   :inventory {:max-plates 4
               :valid-core-types [:matrix-core]
               :valid-plate-types [:matrix-plate]
               :core-min-level 1
               :core-max-level 5
               :plate-min-level 1
               :plate-max-level 3}
   
   :security {:default-password ""
              :max-password-length 16
              :password-change-cooldown-ticks 1200
              :login-attempts-before-lockout 5
              :lockout-duration-ticks 6000}
   
   :rendering {:idle-particle-count 5
               :active-particle-count 20
               :core-rotation-speed 1.0
               :plate-rotation-speed 0.5
               :core-hover-amplitude 0.1
               :plate-hover-amplitude 0.05
               :core-color [0.2 0.8 1.0]
               :plate-color [0.1 0.6 0.9]
               :beam-width 0.05}
   
   :gui {:energy-bar-color [0.2 0.8 1.0]
         :status-indicator-active-color [0.0 1.0 0.0]
         :status-indicator-inactive-color [1.0 0.0 0.0]
         :password-field-width 80
         :refresh-interval-ticks 5}
   
   :debug {:enable-logging false
           :log-energy-updates false
           :log-network-events true
           :render-network-bounds false
           :render-node-connections true}})

;; Configuration file path relative to the config directory
(def config-filename "academy/matrix_block.edn")

(defn get-config-path []
  "Get the absolute path to the configuration file."
  (str (io/file (System/getProperty "user.dir") "config" config-filename)))

(defn save-config-to-file
  "Save configuration to file."
  [config]
  (utils/with-error-handling "Error saving matrix configuration"
    (let [config-path (get-config-path)]
      (io/make-parents config-path)
      (spit config-path (pr-str config))
      (log/info "Saved matrix configuration to" config-path)
      true)))

(defn load-config-from-file
  "Load configuration from file, falling back to defaults if needed."
  []
  (utils/with-error-handling "Error loading matrix configuration"
    (let [config-path (get-config-path)]
      (if (.exists (io/file config-path))
        (let [loaded-config (edn/read-string (slurp config-path))]
          (log/info "Loaded matrix configuration from" config-path)
          (utils/deep-merge default-config loaded-config))
        (do
          (log/info "No matrix configuration found, using defaults")
          default-config)))))

(defn get-config-value
  "Get a configuration value by path, with an optional default."
  [config path & [default]]
  (get-in config path default))

(defn create-config-manager
  "Create a new configuration manager."
  []
  (atom (load-config-from-file)))

(defn get-config
  "Get the full configuration."
  [config-manager]
  @config-manager)

(defn update-config!
  "Update configuration with a function."
  [config-manager f & args]
  (apply swap! config-manager f args))

(defn set-config-value!
  "Set a configuration value at path."
  [config-manager path value]
  (swap! config-manager assoc-in path value))

(defn save-config
  "Save the current configuration to file."
  [config-manager]
  (save-config-to-file @config-manager))

(defn reload-config
  "Reload configuration from file."
  [config-manager]
  (reset! config-manager (load-config-from-file)))