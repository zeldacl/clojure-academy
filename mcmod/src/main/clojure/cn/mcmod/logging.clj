(ns cn.mcmod.logging
  (:require [clojure.string :as str])
  ;(:import [org.apache.logging.log4j LogManager])
  )

;; Replace direct initialization with an atom that can be set later
(def logger (atom nil))

;; Function to initialize the logger from the Forge mod
(defn set-logger! [log-instance]
  (reset! logger log-instance))

;; Helper function to ensure we have a logger or provide a reasonable fallback
(defn- get-logger []
  (if-let [current-logger @logger]
    current-logger
    (do
      (println "WARNING: Logger not initialized, using System.out as fallback")
      (proxy [Object] []
        (info [msg] (println "[INFO]" msg))
        (warn [msg] (println "[WARN]" msg))
        (debug [msg] (println "[DEBUG]" msg))
        (error [msg] (println "[ERROR]" msg))))))

(defn format-msg [msg & args]
  (if (empty? args)
    msg
    (apply format msg args)))

(defn info [msg & args]
  (.info (get-logger) (apply format-msg msg args)))

(defn warn [msg & args]
  (.warn (get-logger) (apply format-msg msg args)))

(defn debug [msg & args]
  (.debug (get-logger) (apply format-msg msg args)))

(defn error [msg & args]
  (.error (get-logger) (apply format-msg msg args)))

(defmacro with-error-logging [& body]
  `(try
     ~@body
     (catch Exception e#
       (error "Error occurred: %s\n%s"
              (.getMessage e#)
              (with-out-str (-> e# .getStackTrace)))
       (throw e#))))
