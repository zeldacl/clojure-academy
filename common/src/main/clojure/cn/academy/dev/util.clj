(ns cn.academy.dev.util
  (:require [cn.academy.core.util.logging :refer [log-info log-debug]]
            [cn.academy.core.util.diagnostics :as diag]
            [cn.academy.core.util.profiling :as prof])
  (:import [java.io File]))

(defn reload-mod! []
  (log-info "Reloading mod...")
  ;; Add reload logic here
  :reloaded)

(defn dump-diagnostics! []
  (let [file (File. "logs/academy-dev-diagnostics.log")]
    (diag/write-diagnostic-report! file)
    (log-info "Wrote diagnostic report to" (.getPath file))))

(defn start-profiling! [& categories]
  (doseq [category (or (seq categories) ["energy" "network" "world"])]
    (prof/start-profiling! category)
    (log-debug "Started profiling category:" category)))

(defn stop-profiling! [& categories]
  (doseq [category (or (seq categories) ["energy" "network" "world"])]
    (prof/log-profile-data! category)
    (prof/stop-profiling! category)))

(defmacro with-dev-profile [category & body]
  `(prof/with-profiling ~category ~@body))

(defn clear-cache! []
  ;; Add cache clearing logic here
  (log-info "Cleared development caches")
  :cleared)

;; Register additional development commands here
(def dev-commands
  {"reload" reload-mod!
   "diag" dump-diagnostics!
   "profile" start-profiling!
   "unprofile" stop-profiling!
   "clear" clear-cache!})