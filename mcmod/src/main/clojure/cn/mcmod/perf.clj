(ns cn.mcmod.perf
  (:require [cn.mcmod.logging :as log]
            [clojure.string :as str])
  (:import [java.util.concurrent ConcurrentHashMap]))

(def metrics (ConcurrentHashMap.))

(defn start-timing [metric-name]
  (.put metrics metric-name (System/nanoTime)))

(defn end-timing [metric-name]
  (when-let [start-time (.get metrics metric-name)]
    (let [duration (/ (- (System/nanoTime) start-time) 1000000.0)]
      (.remove metrics metric-name)
      duration)))

(defmacro with-timing 
  "Execute body and log the time it took to execute.
   Returns the result of body."
  [metric-name & body]
  `(try
     (start-timing ~metric-name)
     (let [result# (do ~@body)]
       (let [duration# (end-timing ~metric-name)]
         (log/debug "%s: %.2fms" ~metric-name duration#))
       result#)
     (catch Exception e#
       (.remove metrics ~metric-name)
       (log/error e# "Error during timed operation: %s" ~metric-name)
       (throw e#))))

(defn get-all-metrics []
  (into {} (map (fn [[k v]] 
                  [k (/ (- (System/nanoTime) v) 1000000.0)]) 
                (seq metrics))))

;; Performance monitoring
(defn start-profiling! [& categories]
  (doseq [category (or (seq categories) ["energy" "network" "world"])]
    (log/debug "Started profiling category: %s" category)
    (start-timing (str "profile_" category))))

(defn stop-profiling! [& categories]
  (doseq [category (or (seq categories) ["energy" "network" "world"])]
    (let [metric-name (str "profile_" category)
          duration (end-timing metric-name)]
      (log/debug "Stopped profiling category: %s (%.2fms)" category duration))))

(defmacro with-dev-profile 
  "Profile the execution of body under the given category"
  [category & body]
  `(try
     (start-profiling! ~category)
     (let [result# (do ~@body)]
       (stop-profiling! ~category)
       result#)
     (catch Exception e#
       (stop-profiling! ~category)
       (throw e#))))