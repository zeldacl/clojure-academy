(ns cn.academy.core.util.core
  (:require [cn.academy.core.util.logging :refer [with-logging log-debug]])
  (:import [java.util.concurrent ConcurrentHashMap]))

(def ^:private cache-state (ConcurrentHashMap.))

(defn memoize-by-key
  "Memoizes a function using a specific key generator.
   Cached results expire after timeout-ms milliseconds."
  [f key-fn timeout-ms]
  (fn [& args]
    (let [k (apply key-fn args)
          now (System/currentTimeMillis)
          entry (get cache-state k)
          [cached-time cached-val] entry]
      (if (and entry (< (- now cached-time) timeout-ms))
        cached-val
        (let [val (with-logging (str "Computing value for key " k)
                   (apply f args))]
          (.put cache-state k [now val])
          val)))))

(defn chunk-coords->key
  "Convert chunk coordinates to a cache key"
  [world chunk-x chunk-z]
  (str (.dimension world) ":" chunk-x ":" chunk-z))

(defn on-error
  "Executes f, calling error-handler with any exception that occurs"
  [f error-handler]
  (try
    (f)
    (catch Exception e
      (error-handler e))))

(defn debounce
  "Returns a function that will execute f only after delay-ms has passed
   without additional calls"
  [f delay-ms]
  (let [timer (atom nil)]
    (fn [& args]
      (when-let [existing @timer]
        (.cancel existing false))
      (let [new-timer (new java.util.Timer)]
        (reset! timer new-timer)
        (.schedule new-timer
                  (proxy [java.util.TimerTask] []
                    (run []
                      (apply f args)))
                  delay-ms))))))