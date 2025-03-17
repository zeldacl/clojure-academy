(ns cn.academy.core.util.profiling
  (:require [cn.academy.core.util.logging :refer [log-info log-debug]])
  (:import [java.util.concurrent ConcurrentHashMap]))

(def ^:private profiling-data (ConcurrentHashMap.))

(defn start-profiling! [category]
  (.put profiling-data category
        {:start-time (System/nanoTime)
         :samples []}))

(defn stop-profiling! [category]
  (when-let [data (.remove profiling-data category)]
    (assoc data :end-time (System/nanoTime))))

(defn add-sample! [category label]
  (when-let [data (.get profiling-data category)]
    (.put profiling-data category
          (update data :samples conj
                  {:label label
                   :timestamp (System/nanoTime)}))))

(defn with-profiling [category f]
  (try 
    (start-profiling! category)
    (let [result (f)]
      (add-sample! category "completion")
      result)
    (finally
      (stop-profiling! category))))

(defmacro profile [category & body]
  `(with-profiling ~category (fn [] ~@body)))

(defn analyze-profile-data [data]
  (let [start-time (:start-time data)
        samples (:samples data)
        end-time (or (:end-time data) (System/nanoTime))]
    {:duration (/ (- end-time start-time) 1000000.0)
     :samples (map #(assoc % :offset 
                          (/ (- (:timestamp %) start-time) 1000000.0))
                  samples)}))

(defn log-profile-data! [category]
  (when-let [data (.get profiling-data category)]
    (let [analysis (analyze-profile-data data)]
      (log-info "Profile data for" category ":")
      (log-info "Duration:" (:duration analysis) "ms")
      (doseq [sample (:samples analysis)]
        (log-info " -" (:label sample) "at" (:offset sample) "ms")))))