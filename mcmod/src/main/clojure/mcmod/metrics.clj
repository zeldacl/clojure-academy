(ns mcmod.metrics
  (:require [mcmod.logging :as log]
            [mcmod.monitoring :as monitoring])
  (:import [java.util.concurrent.atomic AtomicLong]
           [javax.management DynamicMBean MBeanInfo
                           MBeanAttributeInfo OpenType
                           CompositeType CompositeData]))

(def ^:private registry (monitoring/->MonitoringRegistry))

(defprotocol IMetricCollector
  (record-value [this value] "Record a metric value")
  (get-current [this] "Get current metric value"))

(defrecord Gauge [^AtomicLong value]
  IMetricCollector
  (record-value [this new-value]
    (.set value new-value))
  (get-current [this]
    (.get value))
  
  monitoring/IMonitoredComponent
  (get-metrics [this]
    {:value (.get value)})
  (reset-metrics [this]
    (.set value 0)))

(defrecord Counter [^AtomicLong count]
  IMetricCollector
  (record-value [this _]
    (.incrementAndGet count))
  (get-current [this]
    (.get count))
  
  monitoring/IMonitoredComponent
  (get-metrics [this]
    {:count (.get count)})
  (reset-metrics [this]
    (.set count 0)))

(def metrics-registry (atom {}))

(defn register-metric [name metric-type]
  (let [metric (case metric-type
                 :gauge (->Gauge (AtomicLong.))
                 :counter (->Counter (AtomicLong.)))]
    (swap! metrics-registry assoc name metric)
    (monitoring/register-component registry name metric)
    metric))

(defn get-metric [name]
  (get @metrics-registry name))

(defn record-metric [name value]
  (when-let [metric (get-metric name)]
    (record-value metric value)))

(defn get-metric-value [name]
  (when-let [metric (get-metric name)]
    (get-current metric)))