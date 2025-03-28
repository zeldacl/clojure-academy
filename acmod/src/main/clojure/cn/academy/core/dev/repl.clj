;(ns cn.academy.core.dev.repl
;  (:require [cn.academy.core.util.logging :refer [log-info log-debug log-error]]
;            [cn.academy.core.util.dev :as dev]
;            [cn.academy.core.util.monitoring :as monitoring]
;            [cn.academy.core.config :as config]
;            [mcmod.nbt :as nbt]
;            [mcmod.world :as world]
;            [mcmod.position :as position]
;            [mcmod.capabilities :as cap]
;            [clojure.pprint :refer [pprint]]))
;
;(defn reload-dev! []
;  (dev/with-dev-mode
;    (dev/reload-all!)
;    (monitoring/reset-metrics!)
;    :reloaded))
;
;(defn watch-dev-config! [config-path]
;  (dev/watch-config! (java.io.File. config-path)))
;
;(defn show-metrics []
;  (pprint (monitoring/get-metrics)))
;
;(defn get-block-info [world pos]
;  (let [block-state (world/get-block-state world pos)
;        block (world/get-block block-state)
;        tile (world/get-tile-entity world pos)]
;    {:block (world/get-registry-name block)
;     :meta (world/get-meta-from-state block block-state)
;     :tile-entity (when tile
;                   {:class (class tile)
;                    :nbt (let [nbt-data (nbt/create-compound)]
;                          (world/write-tile-to-nbt tile nbt-data)
;                          nbt-data)})}))
;
;(defn set-cat-engine-config! [options]
;  (config/set-config! [:cat-engine]
;    (merge (config/get-config [:cat-engine] {})
;           options))
;  :updated)
;
;(defn inspect-energy-network [world pos range]
;  (let [center (if (position/is-block-pos? pos)
;                 pos
;                 (position/create-block-pos (:x pos) (:y pos) (:z pos)))
;        nodes (for [x (range (- range) (inc range))
;                   y (range (- range) (inc range))
;                   z (range (- range) (inc range))
;                   :let [check-pos (position/add center x y z)
;                         tile (world/get-tile-entity world check-pos)]
;                   :when (and tile
;                             (cap/has-capability? tile "forge:energy" nil))]
;               {:pos check-pos
;                :energy (cap/get-energy-stored
;                         (cap/get-capability tile "forge:energy" nil))})]
;    (doseq [node nodes]
;      (println (format "Energy at %s: %d FE"
;                      (str (:pos node))
;                      (:energy node))))
;    {:total-nodes (count nodes)
;     :total-energy (reduce + (map :energy nodes))}))
