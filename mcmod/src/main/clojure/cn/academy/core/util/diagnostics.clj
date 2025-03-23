(ns cn.academy.core.util.diagnostics
  (:require [cn.academy.core.util.logging :refer [log-info log-error]]
            [cn.academy.core.util.monitoring :as mon]
            [clojure.string :as str])
  (:import [java.util.concurrent ConcurrentHashMap]
           [java.lang.management ManagementFactory]))

(def ^:private diagnostic-data (ConcurrentHashMap.))

(defn collect-system-info! []
  (let [runtime (Runtime/getRuntime)
        mem-bean (ManagementFactory/getMemoryMXBean)
        thread-bean (ManagementFactory/getThreadMXBean)]
    (.put diagnostic-data "system-info"
          {:available-processors (.availableProcessors runtime)
           :max-memory (.maxMemory runtime)
           :free-memory (.freeMemory runtime)
           :total-memory (.totalMemory runtime)
           :heap-usage (.getHeapMemoryUsage mem-bean)
           :non-heap-usage (.getNonHeapMemoryUsage mem-bean)
           :thread-count (.getThreadCount thread-bean)
           :peak-thread-count (.getPeakThreadCount thread-bean)})))

(defn record-diagnostic! [category data]
  (.put diagnostic-data category data))

(defn get-diagnostic-data []
  (into {} diagnostic-data))

(defn log-diagnostic-report! []
  (log-info "=== Diagnostic Report ===")
  (collect-system-info!)
  (doseq [[k v] (get-diagnostic-data)]
    (log-info k ":")
    (log-info v))
  (mon/log-monitor-status!))

(defn get-system-info []
  (let [runtime (Runtime/getRuntime)
        os-bean (ManagementFactory/getOperatingSystemMXBean)
        memory-bean (ManagementFactory/getMemoryMXBean)]
    {:os {:name (System/getProperty "os.name")
          :version (System/getProperty "os.version")
          :arch (System/getProperty "os.arch")}
     :java {:version (System/getProperty "java.version")
            :vendor (System/getProperty "java.vendor")}
     :runtime {:processors (.availableProcessors runtime)
               :max-memory (.maxMemory runtime)
               :total-memory (.totalMemory runtime)
               :free-memory (.freeMemory runtime)}
     :memory {:heap-usage (.getHeapMemoryUsage memory-bean)
              :non-heap-usage (.getNonHeapMemoryUsage memory-bean)}}))

(defn write-diagnostic-report! [file]
  (let [system-info (get-system-info)
        report-lines [(str "=== Academy Diagnostic Report ===")
                     (str "Generated: " (java.util.Date.))
                     ""
                     "=== System Information ==="
                     (str "OS: " (get-in system-info [:os :name]))
                     (str "Java: " (get-in system-info [:java :version]))
                     (str "Memory: " (/ (get-in system-info [:runtime :max-memory]) 1024 1024) "MB")
                     ""
                     "=== Performance Statistics ==="
                     ;; Add performance stats here
                     ]]
    (spit file (str/join "\n" report-lines))
    (log-info "Wrote diagnostic report to" (.getPath file))))

(defn format-throwable [t]
  (let [sw (java.io.StringWriter.)
        pw (java.io.PrintWriter. sw)]
    (.printStackTrace t pw)
    (str sw)))

(defn log-diagnostic-event! [category event-type data]
  (log-error "Diagnostic event:" category event-type)
  (when (instance? Throwable data)
    (log-error (format-throwable data))))