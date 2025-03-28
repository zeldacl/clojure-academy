(ns cn.mcmod.logging
  (:import [org.apache.logging.log4j LogManager Level]
           [java.util.function Supplier]))

;; Logger will be initialized during mod initialization
(def ^:private logger (atom nil))

;; Function to initialize the logger during mod initialization
(defn init-logger! [name]
  (reset! logger (LogManager/getLogger name)))

(defn- format-message [message & args]
  (if (seq args)
    (apply format message args)
    message))

;; All log functions need to check if logger is initialized
(defn debug
  "Log a debug message"
  ([message]
   (when @logger
     (.debug @logger message)))
  ([message & args]
   (when @logger
     (.debug @logger ^String (format-message message args)))))

(defn debug-ex
  "Log a debug message with exception"
  [exception message & args]
  (when @logger
    (.debug @logger ^String (format-message message args) exception)))

(defn info
  "Log an info message"
  ([message]
   (when @logger
     (.info @logger message)))
  ([message & args]
   (when @logger
     (.info @logger ^String (format-message message args)))))

(defn info-ex
  "Log an info message with exception"
  [exception message & args]
  (when @logger
    (.info @logger ^String (format-message message args) exception)))

(defn warn
  "Log a warning message"
  ([message]
   (when @logger
     (.warn @logger message)))
  ([message & args]
   (when @logger
     (.warn @logger ^String (format-message message args)))))

(defn warn-ex
  "Log a warning message with exception"
  [exception message & args]
  (when @logger
    (.warn @logger ^String (format-message message args) exception)))

(defn error
  "Log an error message"
  ([message]
   (when @logger
     (.error @logger message)))
  ([message & args]
   (when @logger
     (.error @logger ^String (format-message message args)))))

(defn error-ex
  "Log an error message with exception"
  [exception message & args]
  (when @logger
    (.error @logger ^String (format-message message args) exception)))

(defn fatal
  "Log a fatal message"
  ([message]
   (when @logger
     (.fatal @logger message)))
  ([message & args]
   (when @logger
     (.fatal @logger ^String (format-message message args)))))

(defn fatal-ex
  "Log a fatal message with exception"
  [exception message & args]
  (when @logger
    (.fatal @logger ^String (format-message message args) exception)))

(defn is-debug-enabled? []
  (when @logger
    (.isDebugEnabled @logger)))

(defn is-info-enabled? []
  (when @logger
    (.isInfoEnabled @logger)))

(defn set-level! [level]
  (case level
    :debug Level/DEBUG
    :info Level/INFO
    :warn Level/WARN
    :error Level/ERROR
    :fatal Level/FATAL
    Level/INFO))
