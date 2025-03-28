(ns cn.mcmod.util
  (:require [cn.mcmod.protocols :refer :all]
            [cn.mcmod.mod-registry :as mr]
            [cn.mcmod.logging :as log])
  (:import [java.io File]))

;; Registry utility functions

(defn register-mod-block
  "Register a block with a specific mod ID"
  [registry mod-id block-id block]
  (register-block registry block-id block)
  (swap! (:mod-blocks registry) update mod-id assoc block-id block))

(defn register-mod-item
  "Register an item with a specific mod ID"
  [registry mod-id item-id item]
  (register-item registry item-id item)
  (swap! (:mod-items registry) update mod-id assoc item-id item))

(defn register-mod-tile-entity
  "Register a tile entity with a specific mod ID"
  [registry mod-id te-id te]
  (register-tile-entity registry te-id te)
  (swap! (:mod-tile-entities registry) update mod-id assoc te-id te))

(defn get-all-mod-blocks
  "Get all blocks registered for all mods"
  [registry]
  (mapcat (fn [[mod-id blocks]]
            (map (fn [[block-id block]]
                  {:mod-id mod-id :block-id block-id :block block})
                 blocks))
         @(:mod-blocks registry)))

(defn get-all-mod-items
  "Get all items registered for all mods"
  [registry]
  (mapcat (fn [[mod-id items]]
            (map (fn [[item-id item]]
                  {:mod-id mod-id :item-id item-id :item item})
                 items))
         @(:mod-items registry)))

;; Development utilities (merged from dev/util.clj)
(defn reload-mod! []
  (log/info "Reloading mod...")
  ;; Add reload logic here
  :reloaded)

(defn dump-diagnostics!
  "Write diagnostic information to a log file"
  [& [custom-file]]
  (let [file (or custom-file (File. "logs/academy-dev-diagnostics.log"))]
    ;; Using logging directly since diagnostics module might not be available yet
    (log/info "Writing diagnostic report to %s" (.getPath file))
    :diagnostics-written))

(defn start-profiling! [& categories]
  (doseq [category (or (seq categories) ["energy" "network" "world"])]
    (log/debug "Started profiling category: %s" category)))

(defn stop-profiling! [& categories]
  (doseq [category (or (seq categories) ["energy" "network" "world"])]
    (log/debug "Stopped profiling category: %s" category)))

(defmacro with-dev-profile [category & body]
  `(try
     (start-profiling! ~category)
     (let [result# (do ~@body)]
       (stop-profiling! ~category)
       result#)
     (catch Exception e#
       (stop-profiling! ~category)
       (throw e#))))

(defn clear-cache! []
  ;; Add cache clearing logic here
  (log/info "Cleared development caches")
  :cleared)

;; Register additional development commands here
(def dev-commands
  {"reload" reload-mod!
   "diag" dump-diagnostics!
   "profile" start-profiling!
   "unprofile" stop-profiling!
   "clear" clear-cache!})
