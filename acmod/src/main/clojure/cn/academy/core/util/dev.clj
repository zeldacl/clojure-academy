(ns cn.academy.core.util.dev
  (:require [cn.academy.core.util.logging :refer [log-info log-debug log-error]]
            [cn.academy.core.config :as config]
            [cn.academy.core.util.monitoring :as monitoring])
  (:import [java.io File]))

(def ^:private watch-state (atom {:watchers {}}))

(defn watch-file! [^File file callback]
  (let [last-modified (atom (.lastModified file))
        watcher-thread
        (doto (Thread.
                #(try
                   (while true
                     (Thread/sleep 1000)
                     (let [current (.lastModified file)]
                       (when (> current @last-modified)
                         (reset! last-modified current)
                         (try
                           (callback file)
                           (catch Throwable t
                             (log-error t "Error in file watcher callback"))))))
                   (catch InterruptedException _
                     (log-debug "File watcher interrupted")))
                (str "file-watcher-" (.getName file)))
          (.setDaemon true)
          (.start))]
    (swap! watch-state update :watchers assoc file watcher-thread)
    watcher-thread))

(defn stop-file-watcher! [^File file]
  (when-let [thread (get-in @watch-state [:watchers file])]
    (.interrupt thread)
    (swap! watch-state update :watchers dissoc file)))

(defn watch-config! [^File config-file]
  (log-info "Starting config file watcher for" (.getName config-file))
  (watch-file! config-file
    (fn [_]
      (try
        (config/reload-config!)
        (log-info "Config reloaded successfully")
        (catch Exception e
          (log-error e "Failed to reload config"))))))

(defn reload-all! []
  (try
    (config/reload-config!)
    (monitoring/reset-metrics!)
    (log-info "Development reload complete")
    :ok
    (catch Exception e
      (log-error e "Development reload failed")
      :error)))

(defmacro with-dev-mode [& body]
  `(try
     (log-debug "Running in development mode")
     (monitoring/set-threshold! "dev-op" 100)
     ~@body
     (catch Exception e#
       (log-error e# "Development operation failed")
       (throw e#))))