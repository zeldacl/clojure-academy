(ns cn.academy.block.config
  (:require [mcmod.protocols :refer [IConfig]]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

;; Configuration state tracking
(def config-state
  (atom {:current nil
         :defaults {}
         :validators {}}))

;; Configuration implementation
(defrecord BlockConfig [state-atom]
  IConfig
  (load-config! [_ path]
    (try
      (let [config (-> path
                      io/reader
                      java.io.PushbackReader.
                      edn/read)]
        (swap! state-atom assoc :current config)
        true)
      (catch Exception e
        (log/error "Failed to load config:" (.getMessage e))
        false)))
  
  (save-config! [_ path]
    (try
      (with-open [w (io/writer path)]
        (binding [*out* w]
          (pr (:current @state-atom)))
        true)
      (catch Exception e
        (log/error "Failed to save config:" (.getMessage e))
        false)))
  
  (get-value [_ path]
    (or (get-in (:current @state-atom) path)
        (get-in (:defaults @state-atom) path)))
  
  (set-value! [_ path value]
    (when-let [validator (get-in @state-atom [:validators path])]
      (when-not (validator value)
        (throw (ex-info "Invalid config value" 
                       {:path path :value value}))))
    (swap! state-atom update :current assoc-in path value))
  
  (register-default! [_ path value]
    (swap! state-atom assoc-in [:defaults path] value))
  
  (register-validator! [_ path validator]
    (swap! state-atom assoc-in [:validators path] validator)))

;; Factory functions
(defn create-config []
  (->BlockConfig config-state))

;; Default configuration values
(def default-config
  {:machine {:max-energy 10000
            :energy-rate 100
            :update-interval 20}
   :network {:max-nodes 64
            :packet-size 1024
            :timeout 5000}
   :fluid {:tank-capacity 16000
          :transfer-rate 100
          :viscosity 1.0}
   :world {:chunk-size 16
          :render-distance 8
          :tick-interval 50}
   :system {:cache-size 1000
           :log-level :info
           :metrics-interval 60}})

;; Configuration validators
(def config-validators
  {:machine {:max-energy #(and (number? %) (pos? %))
            :energy-rate #(and (number? %) (pos? %))
            :update-interval #(and (number? %) (pos? %))}
   :network {:max-nodes #(and (integer? %) (<= % 256))
            :packet-size #(and (integer? %) (<= % 16384))
            :timeout #(and (number? %) (>= % 1000))}
   :fluid {:tank-capacity #(and (number? %) (pos? %))
          :transfer-rate #(and (number? %) (pos? %))
          :viscosity #(and (number? %) (>= % 0.1) (<= % 10.0))}
   :world {:chunk-size #(and (integer? %) (pos? %))
          :render-distance #(and (integer? %) (>= % 2) (<= % 32))
          :tick-interval #(and (number? %) (>= % 20))}
   :system {:cache-size #(and (integer? %) (pos? %))
           :log-level #{:debug :info :warn :error}
           :metrics-interval #(and (number? %) (>= % 10))}})

;; Helper functions
(defn deep-merge [a b]
  (if (map? a)
    (merge-with deep-merge a b)
    b))

(defn validate-config [config validators]
  (doseq [[section sec-vals] validators
          [key validator] sec-vals]
    (when-let [value (get-in config [section key])]
      (when-not (validator value)
        (throw (ex-info "Invalid config value"
                       {:section section
                        :key key
                        :value value}))))))

;; Public API functions
(defn get-config 
  ([] (:current @config-state))
  ([path] (get-in (:current @config-state) path)))

(defn update-config! [f & args]
  (let [new-config (apply f (:current @config-state) args)]
    (validate-config new-config config-validators)
    (swap! config-state assoc :current new-config)))

;; Initialize configuration system
(defn init-config! 
  ([] (init-config! "config/block-config.edn"))
  ([path]
   (reset! config-state {:current {}
                        :defaults default-config
                        :validators config-validators})
   
   (let [config (create-config)]
     ;; Load config file if it exists
     (if (.exists (io/file path))
       (.load-config! config path)
       ;; Otherwise use defaults
       (swap! config-state update :current 
              #(deep-merge % default-config)))
     
     ;; Validate loaded/default config
     (validate-config (:current @config-state) config-validators))))