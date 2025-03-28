(ns cn.mcmod.dev.util
  (:require [cn.mcmod.logging :as log]
            [cn.mcmod.util.diagnostics :as diag])
  (:import [java.io File]))

;; Development utility functions

(defn reload-mod! []
  (log/info "Reloading mod...")
  ;; Call into the REPL namespace to handle reloading
  (require 'cn.mcmod.dev.repl :reload)
  (let [reload-fn (resolve 'cn.mcmod.dev.repl/perform-reload)]
    (when reload-fn
      (reload-fn)))
  :reloaded)

(defn dump-diagnostics!
  "Write diagnostic information to a log file"
  [& [custom-file]]
  (let [file (or custom-file (File. "logs/academy-dev-diagnostics.log"))]
    (log/info "Writing diagnostic report to %s" (.getPath file))
    (diag/generate-diagnostics-report file)
    :diagnostics-written))

(defn start-profiling! []
  (log/info "Starting performance profiling...")
  (require 'cn.mcmod.perf)
  (let [start-fn (resolve 'cn.mcmod.perf/start-profiling)]
    (when start-fn
      (start-fn)))
  :profiling-started)

(defn clear-cache! []
  ;; Clear caches - implemented in respective cache-using modules
  (doseq [ns ['cn.mcmod.render.cache
              'cn.mcmod.world.chunk-cache
              'cn.mcmod.resource.cache]]
    (try
      (require ns :reload)
      (when-let [clear-fn (ns-resolve ns 'clear-cache!)]
        (clear-fn))
      (catch Exception e
        (log/warn e "Failed to clear cache in %s" ns))))
  (log/info "Cleared development caches")
  :cleared)