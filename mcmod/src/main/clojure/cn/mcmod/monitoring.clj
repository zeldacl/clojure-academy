;(ns cn.mcmod.monitoring
;  (:require [cn.mcmod.logging :as log])
;  (:import [javax.management DynamicMBean MBeanInfo
;                           MBeanAttributeInfo MBeanServer
;                           ObjectName StandardMBean]
;           [java.lang.management ManagementFactory]
;           [java.util.concurrent ThreadPoolExecutor]))
;
;(defprotocol IMonitoredComponent
;  (get-metrics [this] "Get current metrics for this component")
;  (reset-metrics [this] "Reset metrics to initial state"))
;
;(defprotocol IMonitoringRegistry
;  (register-component [this component-name component] "Register a component for monitoring")
;  (unregister-component [this component-name] "Unregister a component"))
;
;(def mbean-domain "cn.mcmod.monitoring")
;
;(defn register-mbean
;  "Register a component's metrics as JMX MBean"
;  [component name]
;  (let [object-name (ObjectName. (str mbean-domain ":type=" name))]
;    (try
;      (let [mbean (create-dynamic-mbean name component)
;            server (ManagementFactory/getPlatformMBeanServer)]
;        (.registerMBean server mbean object-name)
;        (log/info "Registered MBean for %s" name)
;        object-name)
;      (catch Exception e
;        (log/error-ex e "Failed to register MBean for %s" name)
;        nil))))
;
;(defn unregister-mbean
;  "Unregister a component's metrics MBean"
;  [name]
;  (try
;    (let [object-name (ObjectName. (str mbean-domain ":type=" name))
;          server (ManagementFactory/getPlatformMBeanServer)]
;      (.unregisterMBean server object-name)
;      (log/info "Unregistered MBean for %s" name)
;      true)
;    (catch Exception e
;      (log/error-ex e "Failed to unregister MBean for %s" name)
;      false)))
;
;(defrecord ThreadPoolMetrics [^ThreadPoolExecutor executor]
;  IMonitoredComponent
;  (get-metrics [this]
;    {:active-count (.getActiveCount executor)
;     :completed-task-count (.getCompletedTaskCount executor)
;     :core-pool-size (.getCorePoolSize executor)
;     :largest-pool-size (.getLargestPoolSize executor)
;     :pool-size (.getPoolSize executor)
;     :task-count (.getTaskCount executor)})
;
;  (reset-metrics [this]
;    ;; ThreadPoolExecutor metrics can't be reset
;    nil))
;
;(defn create-thread-pool-metrics [executor]
;  (->ThreadPoolMetrics executor))
;
;(defrecord MonitoringRegistry []
;  Object
;  (toString [this]
;    "MonitoringRegistry")
;  IMonitoringRegistry
;  (register-component [this component-name component]
;    (register-mbean component component-name))
;
;  (unregister-component [this object-name]
;    (unregister-mbean object-name)))
;
;(defn- create-mbean-info [metric-name attributes]
;  (MBeanInfo.
;   (str "ClojureAcademy:type=" metric-name)
;   "Dynamic MBean for metric monitoring"
;   (into-array MBeanAttributeInfo
;               (for [[attr-name attr-value] attributes]
;                 (MBeanAttributeInfo.
;                  (name attr-name)
;                  (if (number? attr-value)
;                    "java.lang.Long"
;                    "java.lang.String")
;                  (str "Metric attribute " (name attr-name))
;                  true   ; isReadable
;                  false  ; isWritable
;                  false  ; isIs
;                  )))))
;
;(defn- create-dynamic-mbean [metric-name component]
;  (proxy [StandardMBean] [DynamicMBean]
;    (getAttribute [attribute]
;      (get (get-metrics component) (keyword attribute)))
;
;    (setAttribute [attribute]
;      (throw (UnsupportedOperationException.)))
;
;    (getAttributes [attributes]
;      (throw (UnsupportedOperationException.)))
;
;    (setAttributes [attributes]
;      (throw (UnsupportedOperationException.)))
;
;    (getMBeanInfo []
;      (create-mbean-info metric-name (get-metrics component)))))
;
;(defn log-monitor-status! []
;  (log/info "=== Monitoring Status ===")
;  (log/info "JMX Domain: %s" mbean-domain))
;
;;; Singleton registry
;(def global-registry (->MonitoringRegistry))
