(ns mcmod.perf
  (:require [clojure.string :as str])
  (:import [java.util.concurrent ConcurrentHashMap]))

(def metrics (ConcurrentHashMap.))

(defn start-timing [metric-name]
  (.put metrics metric-name (System/nanoTime)))

(defn end-timing [metric-name]
  (when-let [start-time (.get metrics metric-name)]
    (let [duration (/ (- (System/nanoTime) start-time) 1000000.0)]
      (.remove metrics metric-name)
      duration)))

(defmacro with-timing [metric-name & body]
  `(try
     (start-timing ~metric-name)
     (let [result# (do ~@body)]
       (println (str ~metric-name ": " (end-timing ~metric-name) "ms"))
       result#)
     (catch Exception e#
       (.remove metrics ~metric-name)
       (throw e#))))