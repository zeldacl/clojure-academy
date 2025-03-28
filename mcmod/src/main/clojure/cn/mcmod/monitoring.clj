;(ns cn.mcmod.monitoring
;  (:require [cn.mcmod.logging :as log]
;            [clojure.java.jmx :as jmx])
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
;(def mbean-domain "mcmod.monitoring")
;
;(defn register-mbean
;  "Register a component's metrics as JMX MBean"
;  [component name]
;  (let [object-name (ObjectName. (str mbean-domain ":type=" name))]
;    (jmx/register-mbean
;     {:object-name object-name
;      :metrics (get-metrics component)})))
;
;(defn unregister-mbean
;  "Unregister a component's metrics MBean"
;  [name]
;  (jmx/unregister-mbean
;   (ObjectName. (str mbean-domain ":type=" name))))
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
;    (try
;      (let [object-name (register-mbean component-name component)]
;        (log/info "Registered monitoring component: %s" component-name)
;        object-name)
;      (catch Exception e
;        (log/error "Failed to register component %s: %s"
;                  component-name (.getMessage e))
;        nil)))
;
;  (unregister-component [this object-name]
;    (try
;      (unregister-mbean object-name)
;      (log/info "Unregistered monitoring component: %s" (.toString object-name))
;      true
;      (catch Exception e
;        (log/error "Failed to unregister component %s: %s"
;                  (.toString object-name) (.getMessage e))
;        false))))
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
;                  )))
;   nil nil nil))
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
;(defn register-component [registry name component]
;  (try
;    (let [mbean (create-dynamic-mbean name component)
;          server (ManagementFactory/getPlatformMBeanServer)
;          mbean-name (ObjectName. (str "ClojureAcademy:type=" name))]
;      (.registerMBean server mbean mbean-name)
;      (log/info "Registered MBean for %s" name))
;    (catch Exception e
;      (log/error e "Failed to register MBean for %s" name))))
