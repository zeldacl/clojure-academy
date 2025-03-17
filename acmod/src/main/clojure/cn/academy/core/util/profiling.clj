(ns cn.academy.core.util.profiling
  (:require [cn.academy.core.util.logging :refer [log-debug log-warn]]
            [cn.academy.core.util.monitoring :as monitoring])
  (:import [java.util.concurrent ConcurrentHashMap]
           [java.util.concurrent.atomic AtomicLong]))

(def ^:private profiling-data (ConcurrentHashMap.))
(def ^:private sample-counts (ConcurrentHashMap.))

(defn start-profiling! [category]
  (when-not (.containsKey profiling-data category)
    (.put profiling-data category (ConcurrentHashMap.))
    (.put sample-counts category (AtomicLong. 0)))
  :started)

(defn stop-profiling! [category]
  (when (.containsKey profiling-data category)
    (.remove profiling-data category)
    (.remove sample-counts category))
  :stopped)

(defmacro profile [category operation & body]
  `(if (.containsKey profiling-data ~category)
     (let [start# (System/nanoTime)
           result# (do ~@body)
           duration# (/ (- (System/nanoTime) start#) 1000000.0)
           op-data# (.computeIfAbsent
                     (.get profiling-data ~category)
                     ~operation
                     (fn [_#] (AtomicLong. 0)))]
       (.incrementAndGet (.get sample-counts ~category))
       (.addAndGet op-data# (long duration#))
       result#)
     (do ~@body)))

(defn get-profile-data [category]
  (when-let [data (.get profiling-data category)]
    (let [samples (.get (.get sample-counts category))]
      (->> data
           (map (fn [[op times]]
                 [op {:total-ms (.get times)
                     :avg-ms (/ (.get times) samples)
                     :samples samples}]))
           (into {})))))

(defn log-profile-data! [category]
  (when-let [data (get-profile-data category)]
    (log-debug "Profile data for" category ":")
    (doseq [[op stats] data]
      (log-debug (format "  %s: %.2f ms avg over %d samples (total: %.2f ms)"
                        op
                        (:avg-ms stats)
                        (:samples stats)
                        (:total-ms stats))))
    data))

(defn with-profiling* [category f]
  (try
    (start-profiling! category)
    (f)
    (finally
      (log-profile-data! category)
      (stop-profiling! category))))

(defmacro with-profiling [category & body]
  `(with-profiling* ~category (fn [] ~@body)))

;; Register profiling data with monitoring system
(monitoring/set-threshold! "profile-operation" 50)
(add-watch profiling-data :monitor
  (fn [_ _ _ new-state]
    (doseq [[category data] new-state]
      (doseq [[op times] data]
        (let [total (.get times)
              samples (.get (.get sample-counts category))]
          (when (> (/ total samples) 50)
            (log-warn "Slow operation detected -" 
                     category "/" op ":" 
                     (format "%.2f ms avg" (/ total samples)))))))))