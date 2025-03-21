(ns cn.academy.core.util.diagnostics
  (:require [cn.academy.core.util.logging :refer [log-error log-debug]]
            [cn.academy.core.util.monitoring :as monitoring]
            [cn.academy.core.util.profiling :as profiling]
            [mcmod.core :as mccore]
            [clojure.string :as str])
  (:import [java.io StringWriter PrintWriter File]
           [java.util.concurrent.ConcurrentLinkedQueue]))

(def ^:private diagnostic-events (ConcurrentLinkedQueue.))
(def ^:private max-events 1000)

(defn collect-system-info []
  {:java-version (System/getProperty "java.version")
   :os-name (System/getProperty "os.name")
   :os-version (System/getProperty "os.version")
   :os-arch (System/getProperty "os.arch")
   :processors (.availableProcessors (Runtime/getRuntime))
   :total-memory (.totalMemory (Runtime/getRuntime))
   :max-memory (.maxMemory (Runtime/getRuntime))
   :minecraft-version (mccore/get-minecraft-version)})

(defn record-diagnostic-event! [type data]
  (while (> (.size diagnostic-events) max-events)
    (.poll diagnostic-events))
  (.offer diagnostic-events
          {:type type
           :timestamp (System/currentTimeMillis)
           :thread-name (Thread/currentThread)
           :data data}))

(defn get-diagnostic-events []
  (vec diagnostic-events))

(defn format-throwable [^Throwable t]
  (let [sw (StringWriter.)
        pw (PrintWriter. sw)]
    (.printStackTrace t pw)
    (.toString sw)))

(defn create-diagnostic-report []
  (let [system-info (collect-system-info)
        events (get-diagnostic-events)
        metrics (monitoring/get-metrics)
        profiles (into {}
                      (for [category ["energy" "network" "world"]]
                        [category (profiling/get-profile-data category)]))]
    {:system system-info
     :events events
     :metrics metrics
     :profiles profiles}))

(defn write-diagnostic-report! [^File file]
  (try
    (let [report (create-diagnostic-report)]
      (spit file
            (with-out-str
              (println "=== Academy Diagnostic Report ===")
              (println "Generated:" (java.util.Date.))
              (println "\n=== System Information ===")
              (doseq [[k v] (:system report)]
                (println (format "%s: %s" (name k) v)))
              (println "\n=== Performance Metrics ===")
              (doseq [[metric data] (:metrics report)]
                (println (format "%s: %d calls, %.2f ms avg"
                               metric
                               (:count data)
                               (:avg-ms data))))
              (println "\n=== Profile Data ===")
              (doseq [[category profile] (:profiles report)]
                (when profile
                  (println "\nCategory:" category)
                  (doseq [[op stats] profile]
                    (println (format "  %s: %.2f ms avg (%d samples)"
                                   op
                                   (:avg-ms stats)
                                   (:samples stats))))))
              (println "\n=== Recent Events ===")
              (doseq [event (:events report)]
                (println (format "[%s] %s - %s"
                               (.format (java.text.SimpleDateFormat. "HH:mm:ss")
                                      (java.util.Date. (:timestamp event)))
                               (:type event)
                               (pr-str (:data event)))))))
    :ok
    (catch Exception e
      (log-error e "Failed to write diagnostic report")
      :error)))

(defn register-error-handler! []
  (Thread/setDefaultUncaughtExceptionHandler
    (reify Thread$UncaughtExceptionHandler
      (uncaughtException [_ thread ex]
        (log-error ex "Uncaught exception in thread" (.getName thread))
        (record-diagnostic-event! 
          :uncaught-exception
          {:thread (.getName thread)
           :exception (format-throwable ex)})))))