(ns cn.academy.block.env
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

;; Environment configuration
(def env-state
  (atom {:current-env :development
         :configs {}
         :overrides {}}))

;; Default configurations
(def default-configs
  {:development
   {:debug true
    :sync-interval 10
    :performance-monitoring true
    :block-limits {:energy-producers 100
                  :processors 50
                  :multiblocks 20}
    :thresholds {:energy-transfer 1000
                :process-time 200
                :tick-budget 50}}
   
   :production
   {:debug false
    :sync-interval 20
    :performance-monitoring true
    :block-limits {:energy-producers 500
                  :processors 200
                  :multiblocks 100}
    :thresholds {:energy-transfer 5000
                :process-time 100
                :tick-budget 20}}})

;; Configuration loading
(defn load-config! [env-key]
  (try
    (when-let [config-file (io/resource (str "config/" (name env-key) ".edn"))]
      (with-open [r (io/reader config-file)]
        (let [config (edn/read-string (slurp r))]
          (swap! env-state assoc-in [:configs env-key] config)
          true)))
    (catch Exception e
      (log/warn "Failed to load config for env:" env-key (.getMessage e))
      false)))

;; Environment management
(defn set-environment! [env-key]
  (when (contains? default-configs env-key)
    (swap! env-state assoc :current-env env-key)
    (load-config! env-key)))

;; Configuration access
(defn get-config
  ([path]
   (get-config (:current-env @env-state) path))
  ([env-key path]
   (or (get-in (:overrides @env-state) path)
       (get-in (get-in @env-state [:configs env-key]) path)
       (get-in (default-configs env-key) path))))

;; Configuration override management
(defn set-override! [path value]
  (swap! env-state assoc-in [:overrides path] value))

(defn clear-override! [path]
  (swap! env-state update :overrides dissoc path))

(defn clear-all-overrides! []
  (swap! env-state assoc :overrides {}))

;; Block-specific configuration
(defn get-block-config [block-type]
  (let [env (:current-env @env-state)]
    (merge
      (get-config [:blocks :default])
      (get-config [:blocks block-type]))))

;; Environment initialization
(defn init-environment! []
  (try
    (doseq [env (keys default-configs)]
      (load-config! env))
    (log/info "Environment configuration initialized")
    true
    (catch Exception e
      (log/error "Failed to initialize environment:" (.getMessage e))
      false)))