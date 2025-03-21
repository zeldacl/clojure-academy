(ns cn.academy.core.profiling
  (:require [cn.academy.core.monitoring :as monitoring]
            [cn.academy.core.diagnostics :as diagnostics]
            [clojure.tools.logging :as log])
  (:import [java.util.concurrent ConcurrentHashMap]))

;; Profiling context stack
(def ^:dynamic *profiling-context* nil)

;; Profiling results storage
(def results-store (ConcurrentHashMap.))

;; Profile data aggregation
(defrecord ProfileData [category operation]
  Object
  (toString [_]
    (format "[%s] %s" category operation)))

(defn aggregate-profile
  "Aggregate profiling samples"
  [samples]
  (let [grouped (group-by (juxt :category :operation) samples)]
    (into {}
          (for [[[category operation] measurements] grouped]
            (let [times (map :time measurements)
                  count (count times)
                  total (reduce + times)
                  avg (/ total count)
                  max-time (apply max times)
                  min-time (apply min times)]
              [(->ProfileData category operation)
               {:count count
                :total-time total
                :avg-time avg
                :max-time max-time
                :min-time min-time}])))))

;; Profiling macros
(defmacro with-profile
  "Profile execution of body under category and operation"
  [category operation & body]
  `(let [start# (System/nanoTime)
         result# (binding [*profiling-context* 
                          {:category ~category :operation ~operation}]
                  ~@body)
         end# (System/nanoTime)
         time# (/ (- end# start#) 1000000.0)]
     (when *profiling-context*
       (diagnostics/record-sample! ~category
         {:operation ~operation
          :time time#})
       (.put results-store 
             (->ProfileData ~category ~operation)
             time#))
     result#))

(defmacro profile-fn
  "Create profiled version of function"
  [category operation f]
  `(fn [& args#]
     (with-profile ~category ~operation
       (apply ~f args#))))

;; Analysis functions
(defn get-profile-data
  "Get profiling data for category"
  [category]
  (let [data (->> (.entrySet results-store)
                  (filter #(= category 
                            (:category (.getKey ^java.util.Map$Entry %))))
                  (map (fn [^java.util.Map$Entry e]
                         {:profile-data (.getKey e)
                          :time (.getValue e)})))]
    (aggregate-profile data)))

(defn analyze-hotspots
  "Find performance hotspots in category"
  [category threshold]
  (->> (get-profile-data category)
       (filter #(> (:avg-time (val %)) threshold))
       (sort-by #(:avg-time (val %)) >)
       (map (fn [[k v]]
              {:operation (:operation k)
               :avg-time (:avg-time v)
               :count (:count v)}))))

;; Reporting
(defn generate-profile-report
  "Generate profiling report for category"
  [category]
  (let [data (get-profile-data category)
        hotspots (analyze-hotspots category 100)]
    (with-out-str
      (println "=== Profile Report for" category "===")
      (println)
      (println "Performance Hotspots:")
      (doseq [{:keys [operation avg-time count]} hotspots]
        (println (format "  %s: %.2fms avg (%d calls)" 
                        operation avg-time count)))
      (println)
      (println "All Operations:")
      (doseq [[profile-data stats] data]
        (println (format "  %s:" (:operation profile-data)))
        (println (format "    Calls: %d" (:count stats)))
        (println (format "    Avg Time: %.2fms" (:avg-time stats)))
        (println (format "    Min/Max: %.2f/%.2fms" 
                        (:min-time stats) 
                        (:max-time stats)))))))

;; Reset profiling data
(defn reset-profiles! []
  (.clear results-store))

;; Register profiling data with monitoring
(defn register-with-monitoring! []
  (doseq [[profile-data stats] (get-profile-data "energy")]
    (let [collector (monitoring/get-collector "profiling")]
      (.record-metric! collector 
                      (str (:operation profile-data) ".avg")
                      (:avg-time stats)))))