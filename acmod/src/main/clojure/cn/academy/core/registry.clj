(ns cn.academy.core.registry
  (:require [cn.academy.block.block.block-cat-engine :as cat-engine]
            [cn.academy.block.tileentity.tile-cat-engine :as tile-cat-engine]))

(def ^:private registry-state (atom {}))

(defn register-block! [block-id block]
  (swap! registry-state assoc-in [:blocks block-id] block))

(defn register-tile-entity! [tile-id tile-factory]
  (swap! registry-state assoc-in [:tile-entities tile-id] tile-factory))

(defn get-block [block-id]
  (get-in @registry-state [:blocks block-id]))

(defn get-tile-entity-factory [tile-id]
  (get-in @registry-state [:tile-entities tile-id]))

(defn init-registry! []
  (register-block! "cat_engine" (cat-engine/create-cat-engine))
  (register-tile-entity! "cat_engine" tile-cat-engine/create))

(defn clear-registry! []
  (reset! registry-state {}))