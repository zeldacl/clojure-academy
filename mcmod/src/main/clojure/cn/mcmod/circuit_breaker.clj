;(ns cn.mcmod.circuit-breaker
;  (:require [cn.mcmod.logging :as log]
;            [cn.mcmod.monitoring :as monitoring])
;  (:import [java.util.concurrent.atomic AtomicLong AtomicReference]
;           [java.time Instant]))
;
;(defprotocol ICircuitBreaker
;  (allow-execution? [this] "Check if execution is allowed")
;  (record-success [this] "Record successful execution")
;  (record-failure [this] "Record failed execution")
;  (get-state [this] "Get current circuit breaker state")
;  (reset [this] "Reset circuit breaker to initial state"))
;
;(defrecord CircuitBreakerMetrics [failure-count success-count last-failure-time]
;  monitoring/IMonitoredComponent
;  (get-metrics [this]
;    {:failure-count (.get failure-count)
;     :success-count (.get success-count)
;     :last-failure (.toString last-failure-time)})
;
;  (reset-metrics [this]
;    (.set failure-count 0)
;    (.set success-count 0)
;    (.set last-failure-time nil)))
;
;(defrecord CircuitBreaker [failure-threshold reset-timeout-ms
;                          ^AtomicReference state metrics-registry]
;  ICircuitBreaker
;  (allow-execution? [this]
;    (let [current-state (.get state)]
;      (case (:status current-state)
;        :closed true
;        :open (let [timeout-expired? (< (:timeout-until current-state)
;                                       (.toEpochMilli (Instant/now)))]
;                (when timeout-expired?
;                  (.set state {:status :half-open
;                             :failures 0}))
;                timeout-expired?)
;        :half-open true)))
;
;  (record-success [this]
;    (.incrementAndGet (:success-count metrics-registry))
;    (when (= :half-open (:status (.get state)))
;      (.set state {:status :closed
;                  :failures 0})))
;
;  (record-failure [this]
;    (.incrementAndGet (:failure-count metrics-registry))
;    (.set (:last-failure-time metrics-registry) (Instant/now))
;    (let [current-state (.get state)
;          new-failures (inc (:failures current-state))]
;      (when (>= new-failures failure-threshold)
;        (.set state {:status :open
;                    :timeout-until (+ (.toEpochMilli (Instant/now))
;                                    reset-timeout-ms)}))
;      (when (= :half-open (:status current-state))
;        (.set state {:status :open
;                    :timeout-until (+ (.toEpochMilli (Instant/now))
;                                    reset-timeout-ms)}))))
;
;  (get-state [this]
;    (.get state))
;
;  (reset [this]
;    (.set state {:status :closed :failures 0})
;    (monitoring/reset-metrics metrics-registry)))
;
;(defn create-circuit-breaker [& {:keys [failure-threshold reset-timeout-ms]
;                                :or {failure-threshold 5
;                                    reset-timeout-ms 30000}}]
;  (let [metrics (->CircuitBreakerMetrics
;                 (AtomicLong.)
;                 (AtomicLong.)
;                 (AtomicReference.))]
;    (->CircuitBreaker
;      failure-threshold
;      reset-timeout-ms
;      (AtomicReference. {:status :closed :failures 0})
;      metrics)))
