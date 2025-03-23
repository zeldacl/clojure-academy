(ns cn.academy.core.util.monitoring
  (:require [cn.academy.core.util.logging :refer [log-info log-debug log-warn]]
            [clojure.string :as str])
  (:import [java.util.concurrent ConcurrentHashMap]))

(def ^:private thresholds (ConcurrentHashMap.))
(def ^:private monitors (ConcurrentHashMap.))

(defn set-threshold! [category ms]
  (.put thresholds category ms))

(defn get-threshold [category]
  (.get thresholds category))

(defn start-monitor! [category label]
  (.put monitors (str category "-" label)
        {:category category
         :label label
         :start-time (System/nanoTime)}))

(defn stop-monitor! [category label]
  (when-let [data (.remove monitors (str category "-" label))]
    (let [duration (/ (- (System/nanoTime) (:start-time data)) 1000000.0)
          threshold (get-threshold (:category data))]
      (when (and threshold (> duration threshold))
        (log-warn "Operation" (:label data) "in category" (:category data)
                 "took" duration "ms (threshold:" threshold "ms)"))
      duration)))

(defmacro with-monitor [category label & body]
  `(try
     (start-monitor! ~category ~label)
     ~@body
     (finally
       (stop-monitor! ~category ~label))))

(defn get-active-monitors []
  (into {}
        (for [[k v] monitors]
          [k (assoc v :current-duration 
                    (/ (- (System/nanoTime) (:start-time v)) 1000000.0))])))

(defn log-monitor-status! []
  (let [active (get-active-monitors)]
    (when (seq active)
      (log-info "Active monitors:")
      (doseq [[k v] active]
        (log-info " -" (:label v) "(" (:category v) "):"
                 (:current-duration v) "ms")))))