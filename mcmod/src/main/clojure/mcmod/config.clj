(ns mcmod.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(def ^:private config-atom (atom {}))

(defn load-config [file-path]
  (try
    (when (.exists (io/file file-path))
      (reset! config-atom 
              (edn/read-string (slurp file-path))))
    (catch Exception e
      (println "Error loading config:" (.getMessage e)))))

(defn save-config [file-path]
  (try
    (spit file-path (pr-str @config-atom))
    (catch Exception e
      (println "Error saving config:" (.getMessage e)))))

(defn get-config 
  ([] @config-atom)
  ([key] (get @config-atom key)))

(defn set-config! [key value]
  (swap! config-atom assoc key value))

(defn update-config! [key f & args]
  (apply swap! config-atom update key f args))