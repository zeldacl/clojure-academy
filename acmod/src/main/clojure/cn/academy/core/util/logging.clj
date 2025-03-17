(ns cn.academy.core.util.logging
  (:require [clojure.tools.logging :as log]))

(defmacro log-debug [& args]
  `(log/debug ~@args))

(defmacro log-info [& args]
  `(log/info ~@args))

(defmacro log-warn [& args]
  `(log/warn ~@args))

(defmacro log-error [& args]
  `(log/error ~@args))

(defmacro with-logging [operation & body]
  `(try
     (log-debug "Starting" ~operation)
     (let [result# (do ~@body)]
       (log-debug "Completed" ~operation)
       result#)
     (catch Exception e#
       (log-error e# "Failed during" ~operation)
       (throw e#))))