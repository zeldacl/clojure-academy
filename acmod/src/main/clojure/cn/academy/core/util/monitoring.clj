(ns cn.academy.core.util.monitoring
  (:require [cn.academy.core.util.logging :refer [log-debug log-warn]]
            [cn.academy.core.util.async :refer [submit-task]])
  (:import [java.util.concurrent ConcurrentHashMap TimeUnit]))

(def ^:private metrics (ConcurrentHashMap.))
(def ^:private thresholds (ConcurrentHashMap.))

(defn set-threshold! [metric-name threshold-ms]
  (.put thresholds metric-name threshold-ms))

(defn record-timing! [metric-name duration-ms]
  (let [threshold (.get thresholds metric-name Long/MAX_VALUE)]
    (when (> duration-ms threshold)
      (log-warn "Performance warning:" metric-name "took" duration-ms "ms")))
  (.merge metrics metric-name 
          [1 duration-ms]
          (fn [[count total] [new-count new-val]]
            [(+ count new-count) (+ total new-val)])))

(defmacro with-timing [metric-name & body]
  `(let [start# (System/nanoTime)
         result# (do ~@body)
         duration# (/ (- (System/nanoTime) start#) 1000000.0)]
    (record-timing! ~metric-name duration#)
    result#))

(defn get-metrics []
  (into {}
        (map (fn [[k [count total]]]
               [k {:count count
                   :total-ms total
                   :avg-ms (/ total count)}])
             metrics)))

(defn reset-metrics! []
  (.clear metrics))

;; Schedule periodic metric reporting
(submit-task
  #(while true
     (try
       (Thread/sleep 300000) ; Report every 5 minutes
       (let [current-metrics (get-metrics)]
         (when (seq current-metrics)
           (log-debug "Performance metrics:\n"
                     (with-out-str
                       (doseq [[metric {:keys [count avg-ms]}] current-metrics]
                         (println (format "  %s: %d calls, %.2f ms avg" 
                                        metric count avg-ms)))))
           (reset-metrics!)))
       (catch InterruptedException _
         (log-debug "Metric reporting interrupted"))
       (catch Exception e
         (log-warn e "Error reporting metrics")))))