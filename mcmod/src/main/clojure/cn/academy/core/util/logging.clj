(ns cn.academy.core.util.logging
  (:require [clojure.tools.logging :as log]))

(defn log-info
  "Log an info message"
  [& args]
  (log/info (apply str args)))

(defn log-debug
  "Log a debug message"
  [& args]
  (log/debug (apply str args)))

(defn log-warn
  "Log a warning message"
  [& args]
  (log/warn (apply str args)))

(defn log-error
  "Log an error message"
  ([message]
   (log/error message))
  ([throwable message]
   (log/error throwable message)))