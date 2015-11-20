(ns cn.li.utils.log
  ;(:require [clojure.tools [logging :as log]])
  ;(:use [clojure.tools.logging.impl :only [LoggerFactory]])
  (:import (org.apache.logging.log4j LogManager))
  (:import (org.apache.logging.log4j Logger)))

;(print (log4j-factory))
(def *academy-log* (org.apache.logging.log4j.LogManager/getLogger "ClojureAcademy"))
;(alter-var-root (var log/*logger-factory*) (constantly (log4j-factory)))

;
; (;alter-var-root
  ;  #'clojure.tools.logging/*logger-factory*
  ;  (constantly (clojure.tools.logging.log4j/load-factory))
;  )

(defmacro log-info
  [& args]
  ;`(log/info (str "!!!!!" (log4j-factory) "*****" (name log/*logger-factory*)  "------" ~@args))
  `(.log  *academy-log* org.apache.logging.log4j.Level/INFO (str ~@args))
  )

(defmacro log-error
  [e & args]
  `(if e
     (.log  *academy-log* org.apache.logging.log4j.Level/ERROR (str ~@args) e)
     (.log  *academy-log* org.apache.logging.log4j.Level/ERROR (str ~@args)))
  ;`(log/log :error ~e (str ~@args))
  )

(defmacro log-debug
  [& args]
  ;`(log/debug (str ~@args))
  `(.log  *academy-log* org.apache.logging.log4j.Level/DEBUG (str ~@args))
  )

(defmacro log-warn-error
  [e & args]
  ;`(log/warn (str ~@args) ~e)
  )

(defmacro log-warn
  [& args]
  ;`(log/warn (str ~@args))
  )

(defn log-capture!
  [& args]
  ;(apply log/log-capture! args)
  )

(defn log-stream
  [& args]
  ;(apply log/log-stream args)
  )