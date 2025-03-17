(ns cn.academy.core.init
  (:require [cn.academy.api.block :as block-api]
            [cn.academy.api.energy :as energy-api]
            [cn.academy.core.util.logging :refer [log-info log-error with-logging]]
            [cn.academy.core.util.diagnostics :as diag]
            [cn.academy.core.util.version-diagnostics :as vdiag]
            [cn.academy.core.util.monitoring :as monitoring]
            [cn.academy.core.util.profiling :as profiling])
  (:import [java.io File]))

(defn register-block-factory! [forge-factory]
  (block-api/set-forge-factory! forge-factory))

(defn register-energy-impl! [energy-impl]
  (energy-api/set-energy-impl! energy-impl))

(defn init! [forge-factory energy-impl]
  (register-block-factory! forge-factory)
  (register-energy-impl! energy-impl))

(defn init-diagnostics! []
  (with-logging "Initializing diagnostics"
    (diag/register-error-handler!)
    (vdiag/validate-runtime-environment!)
    (monitoring/reset-metrics!)
    
    ;; Start profiling key subsystems
    (doseq [category ["energy" "network" "world"]]
      (profiling/start-profiling! category))
    
    ;; Schedule periodic diagnostic report generation
    (future
      (try
        (while true
          (Thread/sleep (* 30 60 1000)) ; Every 30 minutes
          (diag/write-diagnostic-report!
            (File. "logs/academy-diagnostics.log")))
        (catch InterruptedException _)))))

(defn init-monitoring! []
  (with-logging "Initializing monitoring"
    ;; Set performance thresholds
    (monitoring/set-threshold! "energy-transfer" 50)
    (monitoring/set-threshold! "network-update" 100)
    (monitoring/set-threshold! "world-tick" 50)))

(defn initialize! []
  (try
    (log-info "Initializing Academy Core...")
    (init-diagnostics!)
    (init-monitoring!)
    (log-info "Academy Core initialization complete")
    :ok
    (catch Throwable t
      (log-error t "Failed to initialize Academy Core")
      :error)))