(ns mcmod.cache
  (:require [mcmod.logging :as log]
            [mcmod.monitoring :as monitoring])
  (:import [com.google.common.cache CacheBuilder CacheLoader LoadingCache]
           [java.util.concurrent TimeUnit]))

(defprotocol ICache
  (get-value [this key] "Get a value from the cache")
  (put-value [this key value] "Put a value in the cache")
  (invalidate [this key] "Remove a value from the cache")
  (clear [this] "Clear all entries from the cache"))

(defrecord GuavaCache [^LoadingCache cache loader]
  ICache
  (get-value [this key]
    (try
      (.get cache key)
      (catch Exception e
        (log/warn e "Cache miss for key %s" key)
        nil)))
  
  (put-value [this key value]
    (.put cache key value)
    value)
  
  (invalidate [this key]
    (.invalidate cache key))
  
  (clear [this]
    (.invalidateAll cache))

  monitoring/IMonitoredComponent
  (get-metrics [this]
    (let [stats (.stats cache)]
      {:hit-count (.hitCount stats)
       :miss-count (.missCount stats)
       :request-count (.requestCount stats)
       :eviction-count (.evictionCount stats)
       :average-load-penalty (.averageLoadPenalty stats)}))
  
  (reset-metrics [this]
    (.resetStats (.stats cache))))

(defn create-cache
  "Create a new cache with the specified options:
   :expire-after-minutes - Time after which entries expire
   :maximum-size - Maximum number of entries to store
   :loader - Optional function to load missing values"
  [& {:keys [expire-after-minutes maximum-size loader]}]
  (let [builder (-> (CacheBuilder/newBuilder)
                   (.recordStats))
        builder (if expire-after-minutes
                 (.expireAfterWrite builder expire-after-minutes TimeUnit/MINUTES)
                 builder)
        builder (if maximum-size
                 (.maximumSize builder maximum-size)
                 builder)
        cache-loader (proxy [CacheLoader] []
                      (load [key]
                        (if loader
                          (loader key)
                          (throw (UnsupportedOperationException. "No loader defined")))))
        cache (.build builder cache-loader)]
    (->GuavaCache cache cache-loader)))