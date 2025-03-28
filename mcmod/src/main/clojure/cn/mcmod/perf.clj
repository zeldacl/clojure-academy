(ns cn.mcmod.perf
  (:require [cn.mcmod.logging :as log])
  (:import [java.io File PrintWriter]
           [java.util Date]))

;; Simple performance profiling utilities

(def ^:private active-profiles (atom {}))
(def ^:private profiling-enabled (atom false))

(defn start-profiling! []
  (reset! profiling-enabled true)
  (reset! active-profiles {})
  (log/info "Performance profiling started"))

(defn stop-profiling! []
  (reset! profiling-enabled false)
  (log/info "Performance profiling stopped")
  @active-profiles)

(defn profile-fn 
  "Profile a function call"
  [name f]
  (if @profiling-enabled
    (fn [& args]
      (let [start-time (System/nanoTime)
            result (apply f args)
            end-time (System/nanoTime)
            elapsed (/ (- end-time start-time) 1000000.0)]
        (swap! active-profiles update name 
               (fn [prev] 
                 (if prev
                   (update prev :calls 
                           (fn [calls] 
                             (conj calls elapsed)))
                   {:name name :calls [elapsed]})))
        result))
    f))

(defmacro defn-profiled
  "Define a profiled function"
  [name args & body]
  `(def ~name (profile-fn ~(str name) (fn ~args ~@body))))

(defn get-profile-report []
  (let [profiles @active-profiles]
    (into {} (map (fn [[name data]]
                    [name {:call-count (count (:calls data))
                           :avg-time (if (seq (:calls data))
                                       (/ (reduce + (:calls data))
                                          (count (:calls data)))
                                       0)
                           :min-time (if (seq (:calls data))
                                       (apply min (:calls data))
                                       0)
                           :max-time (if (seq (:calls data))
                                       (apply max (:calls data))
                                       0)}])
                 profiles))))

(defn report-profiles
  "Generate and log a report of all profiled function calls"
  []
  (let [report (get-profile-report)]
    (log/info "Performance Profile Report:")
    (doseq [[name {:keys [call-count avg-time min-time max-time]}] 
            (sort-by (fn [[_ data]] (:avg-time data)) > report)]
      (log/info "%s: %d calls, avg: %.2fms, min: %.2fms, max: %.2fms" 
                name call-count avg-time min-time max-time))))

(defn save-profile-report 
  "Save profile data to a file"
  [file]
  (let [report (get-profile-report)]
    (with-open [writer (PrintWriter. file)]
      (.println writer "=== Academy Mod Performance Profile ===")
      (.println writer (str "Generated: " (Date.)))
      (.println writer "")
      
      (.println writer "Function,Calls,Avg Time (ms),Min Time (ms),Max Time (ms)")
      (doseq [[name {:keys [call-count avg-time min-time max-time]}] 
              (sort-by (fn [[_ data]] (:avg-time data)) > report)]
        (.println writer (format "%s,%d,%.2f,%.2f,%.2f" 
                                name call-count avg-time min-time max-time))))
    (log/info "Profile report saved to %s" (.getPath file))))