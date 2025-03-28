(ns cn.mcmod.logging
  (:import [org.apache.logging.log4j LogManager Level]
           [java.util.function Supplier]))

(def ^:private logger (LogManager/getLogger "ClojureAcademy"))

(defn- format-message [message & args]
  (if (seq args)
    (apply format message args)
    message))

(defn debug
  "Log a debug message"
  ([message]
   (.debug logger message))
  ([message & args]
   (.debug logger ^String (format-message message args))))

(defn debug-ex
  "Log a debug message with exception"
  [exception message & args]
  (.debug logger ^String (format-message message args) exception))

(defn info
  "Log an info message"
  ([message]
   (.info logger message))
  ([message & args]
   (.info logger ^String (format-message message args))))

(defn info-ex
  "Log an info message with exception"
  [exception message & args]
  (.info logger ^String (format-message message args) exception))

(defn warn
  "Log a warning message"
  ([message]
   (.warn logger message))
  ([message & args]
   (.warn logger ^String (format-message message args))))

(defn warn-ex
  "Log a warning message with exception"
  [exception message & args]
  (.warn logger ^String (format-message message args) exception))

(defn error
  "Log an error message"
  ([message]
   (.error logger message))
  ([message & args]
   (.error logger ^String (format-message message args))))

(defn error-ex
  "Log an error message with exception"
  [exception message & args]
  (.error logger ^String (format-message message args) exception))

(defn fatal
  "Log a fatal message"
  ([message]
   (.fatal logger message))
  ([message & args]
   (.fatal logger ^String (format-message message args))))

(defn fatal-ex
  "Log a fatal message with exception"
  [exception message & args]
  (.fatal logger ^String (format-message message args) exception))

(defn is-debug-enabled? []
  (.isDebugEnabled logger))

(defn is-info-enabled? []
  (.isInfoEnabled logger))

(defn set-level! [level]
  (case level
    :debug Level/DEBUG
    :info Level/INFO
    :warn Level/WARN
    :error Level/ERROR
    :fatal Level/FATAL
    Level/INFO))
