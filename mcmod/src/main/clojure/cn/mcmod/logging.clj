(ns mcmod.logging
  (:require [clojure.string :as str])
  (:import [org.apache.logging.log4j LogManager]))

(def logger (LogManager/getLogger "mcmod"))

(defn format-msg [msg & args]
  (if (empty? args)
    msg
    (apply format msg args)))

(defn info [msg & args]
  (.info logger (apply format-msg msg args)))

(defn warn [msg & args]
  (.warn logger (apply format-msg msg args)))

(defn debug [msg & args]
  (.debug logger (apply format-msg msg args)))

(defn error [msg & args]
  (.error logger (apply format-msg msg args)))

(defmacro with-error-logging [& body]
  `(try
     ~@body
     (catch Exception e#
       (error "Error occurred: %s\n%s" 
              (.getMessage e#)
              (with-out-str (-> e# .getStackTrace)))
       (throw e#))))