(ns cn.academy.core.diagnostics
  (:require [cn.academy.core.monitoring :as monitoring]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

;; Diagnostic state
(def diagnostic-state
  (atom {:startup-time nil
         :uptime-ms 0
         :last-report-time nil}))

;; System health checks
(defn check-resource-usage []
  (let [runtime (Runtime/getRuntime)
        max-memory (.maxMemory runtime)
        total-memory (.totalMemory runtime)
        free-memory (.freeMemory runtime)
        used-memory (- total-memory free-memory)
        usage-pct (* 100.0 (/ used-memory max-memory))]
    {:memory {:used used-memory
             :total max-memory
             :usage-pct usage-pct}
     :threads (.activeCount (Thread/currentThread))}))

(defn check-subsystem-health []
  (let [metrics {:energy (monitoring/get-metrics "energy")
                 :network (monitoring/get-metrics "network")
                 :world (monitoring/get-metrics "world")
                 :render (monitoring/get-metrics "render")}]
    {:metrics metrics
     :errors (error/get-error-history 
              (- (System/currentTimeMillis) (* 60 60 1000)) ; Last hour
              (System/currentTimeMillis))}))

;; Report generation
(defn generate-report []
  (let [now (System/currentTimeMillis)
        uptime (- now (:startup-time @diagnostic-state))
        system-health (check-resource-usage)
        subsystem-health (check-subsystem-health)]
    {:timestamp now
     :uptime uptime
     :system system-health
     :subsystems subsystem-health}))

(defn format-report [report]
  (let [timestamp (.format 
                   (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss")
                   (LocalDateTime/now))
        {:keys [uptime system subsystems]} report]
    (with-out-str
      (println "=== AcademyCraft Diagnostic Report ===")
      (println "Time:" timestamp)
      (println "Uptime:" (format "%.2f hours" (/ uptime (* 1000.0 60 60))))
      (println)
      (println "=== System Health ===")
      (println "Memory Usage:" 
               (format "%.1f%% (%.1f MB / %.1f MB)"
                      (get-in system [:memory :usage-pct])
                      (/ (get-in system [:memory :used]) 1048576.0)
                      (/ (get-in system [:memory :total]) 1048576.0)))
      (println "Active Threads:" (get-in system [:threads]))
      (println)
      (println "=== Subsystem Metrics ===")
      (doseq [[subsystem metrics] (get-in subsystems [:metrics])]
        (println)
        (println (str (name subsystem) " Metrics:"))
        (doseq [[metric value] metrics]
          (println (format "  %s: %.2f" (name metric) value))))
      (println)
      (println "=== Recent Errors ===")
      (doseq [error (get-in subsystems [:errors])]
        (println (format "[%s] %s: %s"
                        (.format (DateTimeFormatter/ofPattern "HH:mm:ss")
                                (LocalDateTime/ofInstant
                                  (java.time.Instant/ofEpochMilli (:timestamp error))
                                  (java.time.ZoneId/systemDefault)))
                        (:id error)
                        (get-in error [:error :message])))))))

(defn write-report! [file]
  (let [report (generate-report)]
    (swap! diagnostic-state assoc :last-report-time (:timestamp report))
    (spit file 
          (str (format-report report) "\n\n")
          :append true)))

;; Startup/shutdown reports
(defn write-startup-report! []
  (let [startup-time (System/currentTimeMillis)]
    (swap! diagnostic-state assoc :startup-time startup-time)
    (write-report! "logs/academy-startup.log")))

(defn write-shutdown-report! []
  (write-report! "logs/academy-shutdown.log"))

;; System profiling
(def profiling-state (atom {}))

(defn start-profiling! [category]
  (swap! profiling-state assoc category
         {:start-time (System/nanoTime)
          :samples (atom [])}))

(defn record-sample! [category data]
  (when-let [profiling (get @profiling-state category)]
    (swap! (:samples profiling) conj
           (assoc data
                  :timestamp (- (System/nanoTime)
                              (:start-time profiling))))))

(defn stop-profiling! [category]
  (swap! profiling-state dissoc category))

(defn stop-all-profiling! []
  (reset! profiling-state {}))

;; Initialize profiling categories
(defn init-profiling! []
  (doseq [category ["energy" "network" "world" "render"]]
    (start-profiling! category)))