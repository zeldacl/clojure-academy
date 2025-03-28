;(ns mcmod.concurrent
;  (:require [mcmod.monitoring :as monitoring]
;            [mcmod.logging :as log])
;  (:import [java.util.concurrent ThreadPoolExecutor
;                                ThreadFactory
;                                TimeUnit
;                                LinkedBlockingQueue]
;           [com.google.common.util.concurrent ThreadFactoryBuilder]))
;
;(def ^:private monitoring-registry (monitoring/->MonitoringRegistry))
;
;(defprotocol IThreadPool
;  (submit [this task] "Submit a task to the thread pool")
;  (shutdown [this] "Shutdown the thread pool gracefully")
;  (force-shutdown [this] "Force immediate shutdown"))
;
;(defrecord MonitoredThreadPool [^ThreadPoolExecutor executor name]
;  IThreadPool
;  (submit [this task]
;    (.submit executor task))
;
;  (shutdown [this]
;    (.shutdown executor)
;    (.awaitTermination executor 30 TimeUnit/SECONDS))
;
;  (force-shutdown [this]
;    (.shutdownNow executor))
;
;  monitoring/IMonitoredComponent
;  (get-metrics [this]
;    {:active-threads (.getActiveCount executor)
;     :completed-tasks (.getCompletedTaskCount executor)
;     :core-pool-size (.getCorePoolSize executor)
;     :current-pool-size (.getPoolSize executor)
;     :largest-pool-size (.getLargestPoolSize executor)
;     :max-pool-size (.getMaximumPoolSize executor)
;     :queued-tasks (.getQueue executor)
;     :thread-pool-name name})
;
;  (reset-metrics [this]
;    nil))
;
;(defn create-thread-pool
;  "Create a monitored thread pool with the specified options:
;   :name - Name prefix for threads
;   :core-size - Core number of threads
;   :max-size - Maximum number of threads
;   :queue-size - Size of work queue
;   :keep-alive-minutes - Time to keep idle threads alive"
;  [{:keys [name core-size max-size queue-size keep-alive-minutes]
;    :or {core-size 2
;         max-size 4
;         queue-size 1000
;         keep-alive-minutes 1}}]
;  (let [thread-factory (-> (ThreadFactoryBuilder.)
;                          (.setNameFormat (str name "-%d"))
;                          (.setDaemon true)
;                          (.build))
;        executor (ThreadPoolExecutor.
;                 core-size
;                 max-size
;                 keep-alive-minutes
;                 TimeUnit/MINUTES
;                 (LinkedBlockingQueue. queue-size)
;                 thread-factory)]
;    (->MonitoredThreadPool executor name)))
;
;(def ^:private executor-service
;  (Executors/newFixedThreadPool
;    (max 2 (- (.. Runtime getRuntime availableProcessors) 1))))
;
;(def ^:private thread-pool-metrics
;  (monitoring/create-thread-pool-metrics executor-service))
;
;(defn init-monitoring []
;  (monitoring/register-component monitoring-registry
;                                "ThreadPool"
;                                thread-pool-metrics))
;
;(defmacro with-lock [lock & body]
;  `(let [locked# (try
;                  (.tryLock ~lock 100 TimeUnit/MILLISECONDS)
;                  (catch Exception e# false))]
;     (if locked#
;       (try
;         ~@body
;         (finally
;           (.unlock ~lock)))
;       (log/warn "Failed to acquire lock within timeout"))))
;
;(defn run-async [f]
;  (.submit executor-service
;           (reify java.util.concurrent.Callable
;             (call [this]
;               (try
;                 (f)
;                 (catch Exception e
;                   (log/error "Async task error: %s" (.getMessage e))
;                   nil))))))
;
;(defn shutdown []
;  (.shutdown executor-service)
;  (let [terminated? (.awaitTermination executor-service 5 TimeUnit/SECONDS)]
;    (when-not terminated?
;      (log/warn "Thread pool did not terminate within timeout"))
;    (monitoring/unregister-component monitoring-registry "ThreadPool")))
;
;(defprotocol IAtomicValue
;  (get-value [this])
;  (set-value [this new-value])
;  (update-value [this f]))
;
;(defrecord AtomicValue [^AtomicReference ref]
;  IAtomicValue
;  (get-value [this]
;    (.get ref))
;
;  (set-value [this new-value]
;    (.set ref new-value))
;
;  (update-value [this f]
;    (loop []
;      (let [old-value (.get ref)
;            new-value (f old-value)]
;        (if (.compareAndSet ref old-value new-value)
;          new-value
;          (recur))))))
