(ns cn.academy.core.util.async
  (:require [cn.academy.core.util.logging :refer [log-error log-debug]])
  (:import [java.util.concurrent Executors ThreadFactory TimeUnit]
           [com.google.common.util.concurrent ThreadFactoryBuilder]))

(def ^:private thread-pool
  (let [factory (-> (ThreadFactoryBuilder.)
                    (.setNameFormat "academy-worker-%d")
                    (.setDaemon true)
                    .build)]
    (Executors/newFixedThreadPool 2 factory)))

(defn submit-task [f]
  (try
    (.submit thread-pool
             (reify Runnable
               (run []
                 (try
                   (f)
                   (catch Throwable t
                     (log-error t "Error in async task"))))))
    (catch Throwable t
      (log-error t "Failed to submit async task"))))

(defn with-timeout [f timeout-ms]
  (let [future (.submit thread-pool
                        (reify java.util.concurrent.Callable
                          (call []
                            (f))))]
    (try
      (.get future timeout-ms TimeUnit/MILLISECONDS)
      (catch java.util.concurrent.TimeoutException _
        (log-debug "Task timed out after" timeout-ms "ms")
        (.cancel future true)
        nil)
      (catch Throwable t
        (log-error t "Error executing task with timeout")
        nil))))

(defn shutdown! []
  (.shutdown thread-pool)
  (when-not (.awaitTermination thread-pool 5 TimeUnit/SECONDS)
    (log-debug "Force shutting down thread pool")
    (.shutdownNow thread-pool)))