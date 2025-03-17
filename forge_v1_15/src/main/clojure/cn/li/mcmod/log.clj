(ns cn.li.mcmod.log
  (:require [clojure.tools.logging :as log])
  (:import (org.apache.logging.log4j LogManager)))

;(defn init-log []
;  (alter-var-root #'log/*logger-factory* (constantly (or (clojure.tools.logging.impl/log4j2-factory) log/*logger-factory*) ) ))


;(def ^{:dynamic true} *log* (.getLogger LogManager))
;
;(defn log [& form]
;  (if *log*
;    (.log *log* )))
;
;(defn error [& form]
;  )

;(def log-levels {:all Level/ALL
;                 :trace Level/TRACE
;                 :debug Level/DEBUG
;                 :info Level/INFO
;                 :warn Level/WARN
;                 :error Level/ERROR
;                 :fatal Level/FATAL
;                 :off Level/OFF})
;
;(defn- set-logger-level*
;  [^org.apache.log4j.Logger logger level]
;  {:pre [(log-levels level)]}
;  (.setLevel logger (log-levels level)))
;
;(defn set-root-logger-level!
;  "Sets the root logger to be at `level`."
;  [level]
;  (set-logger-level* (Logger/getRootLogger)
;    level))
;
;(defn set-logger-level!
;  "Sets the specified `logger` to be at `level`."
;  [logger level]
;  (set-logger-level* (Logger/getLogger logger)
;    level))
;
;(defn loggers-seq []
;  (-> (Logger/getRootLogger)
;    .getLoggerRepository
;    .getCurrentLoggers
;    enumeration-seq))
;
;(defn logger-levels [loggers]
;  (into {}
;    (for [logger loggers]
;      [(.getName logger) ((set/map-invert log-levels) (.getEffectiveLevel logger))])))
;
;(defn set-all-loggers-level!
;  "Sets the level of all configured loggers to be at `level`."
;  [level]
;  (doseq [logger (loggers-seq)]
;    (set-logger-level* logger level)))

;(defn reload-config!
;  "Reconfigures log4j from a log4j.properties file on the classpath"
;  []
;  (with-open [config-stream
;              (-> "log4j.properties"
;                io/resource
;                io/file
;                io/input-stream)]
;    (PropertyConfigurator/configure config-stream)))