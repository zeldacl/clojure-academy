(ns cn.academy.core.monitoring
  (:require [mcmod.protocols :refer [IMetricsCollector]]
            [clojure.tools.logging :as log])
  (:import [java.util.concurrent ConcurrentHashMap]))

;; Metrics state
(def metrics-state 
  (atom {:thresholds {}
         :collectors {}
         :metrics (ConcurrentHashMap.)
         :active false}))

;; Metrics collection
(defrecord MetricsCollector [category]
  IMetricsCollector
  (record-metric! [_ name value]
    (when (:active @metrics-state)
      (let [threshold (get-in @metrics-state [:thresholds category])]
        (when (and threshold (> value threshold))
          (log/warn category "metric" name "exceeded threshold:" value)))
      (.put ^ConcurrentHashMap (:metrics @metrics-state)
            (str category "." name)
            value)))
  
  (get-metric [_ name]
    (.get ^ConcurrentHashMap (:metrics @metrics-state)
          (str category "." name))))

;; Monitoring API
(defn set-threshold!
  "Set performance threshold for category"
  [category threshold]
  (swap! metrics-state assoc-in [:thresholds category] threshold))

(defn get-collector
  "Get or create metrics collector for category"
  [category]
  (if-let [existing (get-in @metrics-state [:collectors category])]
    existing
    (let [collector (->MetricsCollector category)]
      (swap! metrics-state assoc-in [:collectors category] collector)
      collector)))

(defn get-metrics
  "Get all metrics for category"
  [category]
  (let [prefix (str category ".")
        metrics (.entrySet ^ConcurrentHashMap (:metrics @metrics-state))]
    (->> metrics
         (filter #(.startsWith ^String (.getKey %) prefix))
         (map (fn [^java.util.Map$Entry e]
                [(.substring ^String (.getKey e) (.length prefix))
                 (.getValue e)]))
         (into {}))))

(defn reset-metrics!
  "Reset all metrics"
  []
  (.clear ^ConcurrentHashMap (:metrics @metrics-state)))

;; Collection control
(defn start-metrics-collection!
  "Start collecting metrics"
  []
  (swap! metrics-state assoc :active true))

(defn stop-metrics-collection!
  "Stop collecting metrics" 
  []
  (swap! metrics-state assoc :active false))

;; Convenience macros
(defmacro with-metrics
  "Record execution time of body"
  [category name & body]
  `(let [collector# (get-collector ~category)
         start# (System/nanoTime)
         result# (do ~@body)
         time# (/ (- (System/nanoTime) start#) 1000000.0)]
     (.record-metric! collector# ~name time#)
     result#))

(defmacro track-rate
  "Track rate of operations"
  [category name window-ms & body]
  `(let [collector# (get-collector ~category)
         window# ~window-ms
         count# (atom 0)
         start# (System/currentTimeMillis)]
     (future
       (while true
         (Thread/sleep window#)
         (let [current# @count#
               rate# (/ (* current# 1000.0) window#)]
           (.record-metric! collector# ~name rate#)
           (reset! count# 0))))
     (try
       (let [result# (do ~@body)]
         (swap! count# inc)
         result#)
       (catch Exception e#
         (swap! count# inc)
         (throw e#)))))