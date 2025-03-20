(ns forge-impl.registry.dependency-scanner
  (:require [mcmod.protocols :refer :all]
            [clojure.tools.logging :as log]
            [mcmod.registry :as mcmod-registry]))

;; Track loaded dependencies
(def ^:private loaded-deps (atom #{}))

;; Check if a dependency is loaded
(defn dependency-loaded? [dep-id]
  (contains? @loaded-deps dep-id))

;; Load a dependency
(defn load-dependency! [dep-id]
  (when-not (dependency-loaded? dep-id)
    (log/info (str "Loading dependency: " dep-id))
    (try
      ;; Load dependency namespace
      (require (symbol dep-id))
      ;; Mark as loaded
      (swap! loaded-deps conj dep-id)
      (log/info (str "Successfully loaded dependency: " dep-id))
      (catch Exception e
        (log/error (str "Failed to load dependency: " dep-id))
        (throw e)))))

;; Load mcmod dependencies
(defn load-mcmod-dependencies! []
  (log/info "Loading mcmod dependencies")
  
  ;; Load core mcmod namespaces
  (load-dependency! 'mcmod.protocols)
  (load-dependency! 'mcmod.registry)
  (load-dependency! 'mcmod.block.block-state)
  
  (log/info "mcmod dependencies loaded"))

;; Main scanning function
(defn scan-dependencies! []
  (log/info "Starting dependency scanning")
  
  ;; Load required dependencies
  (load-mcmod-dependencies!)
  
  ;; Initialize mcmod registry if needed
  (when-not (mcmod-registry/initialized?)
    (mcmod-registry/init!))
  
  (log/info "Dependency scanning complete"))