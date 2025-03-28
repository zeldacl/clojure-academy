(ns cn.mcmod.stats
  (:require [cn.mcmod.logging :as log])
  (:import [java.util.concurrent ConcurrentHashMap]
           [java.util.concurrent.atomic AtomicLong]))

(def stats-registry (ConcurrentHashMap.))

(defprotocol IStatistic
  (increment [this amount] "Increment statistic by amount")
  (get-value [this] "Get current value")
  (reset-value [this] "Reset statistic to initial value"))

(defrecord Statistic [^AtomicLong value]
  IStatistic
  (increment [this amount]
    (.addAndGet value (long amount)))
  
  (get-value [this]
    (.get value))
  
  (reset-value [this]
    (.set value 0)))

(defn register-stat [name]
  (.computeIfAbsent stats-registry name
                    (reify java.util.function.Function
                      (apply [this _]
                        (->Statistic (AtomicLong.))))))

(defn track-stat [name amount]
  (try 
    (let [stat (register-stat name)]
      (increment stat amount)
      (log/debug "Stat %s increased by %d to %d" 
                 name amount (get-value stat)))
    (catch Exception e
      (log/error "Error tracking stat %s: %s" name (.getMessage e)))))

(defn get-stat [name]
  (try
    (get-value (register-stat name))
    (catch Exception e
      (log/error "Error getting stat %s: %s" name (.getMessage e))
      0)))

(defn reset-stat [name]
  (try
    (reset-value (register-stat name))
    (log/debug "Reset stat %s to 0" name)
    (catch Exception e
      (log/error "Error resetting stat %s: %s" name (.getMessage e)))))

(defn get-all-stats []
  (try
    (into {} 
      (map (fn [[k v]] [k (get-value v)]) 
           (seq stats-registry)))
    (catch Exception e
      (log/error "Error getting all stats: %s" (.getMessage e))
      {})))