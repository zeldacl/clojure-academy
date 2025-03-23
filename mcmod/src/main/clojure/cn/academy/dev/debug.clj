(ns cn.academy.dev.debug
  (:require [cn.academy.core.util.logging :refer [log-info log-debug]]
            [cn.academy.core.util.diagnostics :as diag]
            [cn.academy.core.util.monitoring :as mon])
  (:import [java.io File]))

(def debug-state (atom {}))

(defn watch-value! [path pred]
  (swap! debug-state assoc-in [:watches path] pred)
  (log-debug "Added watch for path" path))

(defn unwatch! [path]
  (swap! debug-state update :watches dissoc path)
  (log-debug "Removed watch for path" path))

(defn dump-state! []
  (let [file (File. "logs/academy-debug-state.log")]
    (spit file (pr-str @debug-state))
    (log-info "Dumped debug state to" (.getPath file))))

(defn check-watches! []
  (doseq [[path pred] (get @debug-state :watches)]
    (try
      (when (pred)
        (log-info "Watch triggered for path" path))
      (catch Throwable t
        (log-debug t "Error checking watch" path)))))

(defn track-performance! [category threshold-ms]
  (mon/set-threshold! category threshold-ms)
  (log-debug "Set performance threshold for" category "to" threshold-ms "ms"))

(defn debug-command [cmd & args]
  (case cmd
    "watch" (apply watch-value! args)
    "unwatch" (apply unwatch! args)
    "dump" (dump-state!)
    "perf" (apply track-performance! args)
    (log-debug "Unknown debug command:" cmd)))