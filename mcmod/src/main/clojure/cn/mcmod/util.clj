(ns cn.mcmod.util
  (:require [cn.mcmod.protocols :refer :all]
            [cn.mcmod.registry :as registry]
            [cn.mcmod.logging :as log])
  (:import [java.io File]))

;; Registry utility functions

(defn register-mod-block
  "Register a block with a specific mod ID"
  [registry mod-id block-id block]
  (registry/register-block! registry block-id block)
  (swap! (:mod-blocks registry) update mod-id assoc block-id block))

(defn register-mod-item
  "Register an item with a specific mod ID"
  [registry mod-id item-id item]
  (registry/register-item! registry item-id item)
  (swap! (:mod-items registry) update mod-id assoc item-id item))

(defn register-mod-tile-entity
  "Register a tile entity with a specific mod ID"
  [registry mod-id te-id te]
  (registry/register-tile-entity! registry te-id te)
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

;; Development utilities - moved to cn.mcmod.dev.util
(defn reload-mod! []
  (log/info "Reloading mod...")
  (require 'cn.mcmod.dev.util :reload)
  ((resolve 'cn.mcmod.dev.util/reload-mod!)))

(defn dump-diagnostics!
  "Write diagnostic information to a log file"
  [& [custom-file]]
  (require 'cn.mcmod.dev.util :reload)
  ((resolve 'cn.mcmod.dev.util/dump-diagnostics!) custom-file))

(defn clear-cache! []
  (require 'cn.mcmod.dev.util :reload)
  ((resolve 'cn.mcmod.dev.util/clear-cache!)))

;; Resource utilities
(defn get-resource-path [mod-id resource-type path]
  (str "assets/" mod-id "/" resource-type "/" path))

(defn load-resource [path]
  (-> (Thread/currentThread)
      (.getContextClassLoader)
      (.getResourceAsStream path)))

;; String utilities
(defn format-identifier [mod-id name]
  (str mod-id ":" name))

(defn parse-identifier [identifier]
  (let [[mod-id name] (clojure.string/split identifier #":")]
    {:mod-id mod-id :name name}))
