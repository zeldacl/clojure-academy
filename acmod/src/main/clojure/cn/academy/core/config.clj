(ns cn.academy.core.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

;; Default configuration
(def default-config
  {:cat-engine
   {:energy-gen-rate 5.0
    :max-energy 100000.0
    :wireless-range 16
    :allow-interdimensional true}
   
   :monitoring
   {:sample-interval 1000  ; ms
    :history-size 3600    ; samples
    :thresholds
    {:machine
     {:process-time 5000  ; ms
      :energy-rate 1000   ; units/tick
      :resource-rate 100} ; items/tick
     :network
     {:message-rate 1000  ; msgs/sec
      :packet-size 32768  ; bytes
      :latency 100}      ; ms
     :world
     {:chunk-load 500    ; ms
      :block-updates 10000
      :entity-count 1000}
     :render
     {:frame-time 16     ; ms (60 FPS)
      :batch-count 1000
      :vertex-count 1000000}}}
   
   :diagnostics
   {:report-interval 300000  ; 5 minutes
    :error-history 1000     ; entries
    :log-level :info
    :log-path "logs/academy-diagnostics.log"}
   
   :profiling
   {:enabled true
    :sample-rate 100    ; samples/sec
    :categories
    ["machine"
     "network" 
     "world"
     "render"]}})

;; Configuration state
(def config-state (atom default-config))

;; Configuration file handling
(def config-file "config/academy-monitoring.edn")

(defn load-config! []
  (try
    (when (.exists (io/file config-file))
      (let [config (edn/read-string (slurp config-file))]
        (reset! config-state (merge-with merge default-config config))
        (log/info "Loaded monitoring configuration from" config-file)))
    (catch Exception e
      (log/error "Failed to load configuration:" (.getMessage e))
      (reset! config-state default-config))))

(defn save-config! []
  (try
    (io/make-parents config-file)
    (spit config-file (pr-str @config-state))
    (log/info "Saved monitoring configuration to" config-file)
    true
    (catch Exception e
      (log/error "Failed to save configuration:" (.getMessage e))
      false)))

;; Configuration access
(defn get-config
  "Get configuration value by path"
  [& path]
  (get-in @config-state path))

(defn set-config!
  "Set configuration value"
  [path value]
  (swap! config-state assoc-in path value)
  (save-config!))

;; Threshold checking
(defn check-threshold
  "Check if value exceeds threshold"
  [category metric value]
  (let [threshold (get-in @config-state [:monitoring :thresholds category metric])]
    (when (and threshold (> value threshold))
      {:category category
       :metric metric
       :value value
       :threshold threshold})))

;; Profile category management
(defn enable-profile-category! [category]
  (swap! config-state update-in [:profiling :categories] conj category)
  (save-config!))

(defn disable-profile-category! [category]
  (swap! config-state update-in [:profiling :categories] disj category)
  (save-config!))

;; Initialize configuration
(defn init-config! []
  (load-config!))

(defn get-cat-engine-config []
  (get-config [:cat-engine] {}))