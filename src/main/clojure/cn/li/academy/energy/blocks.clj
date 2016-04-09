(ns cn.li.academy.energy.blocks
  (:require [cn.li.academy.energy.tileentities :refer [new-node-entity get-entity-max-energy tileentity-node]]
            [cn.li.academy.energy.hepler :refer [node-type->id]]
            [cn.li.academy.core.blocks :refer [defACblock defACblockcontainer]]
            [cn.li.academy.tab :refer [tab-clojure-academy]]
            [forge-clj.util :refer [get-tile-entity-at]])
  (:import (net.minecraft.client.renderer.texture IIconRegister)
           (net.minecraft.world IBlockAccess)))

;(def icons (atom {}))
(let [icons (atom {})]
  (defn register-node-block-icons [^IIconRegister reg type]
    (let [names (map (partial str "clojureacademy:node_" type "_side_" ) (range 5))
          register-icon (fn [iname]
                          (.registerIcon reg iname))
          temp-icons (doall (map register-icon names))
          icon-map (zipmap (range 5) temp-icons)
          icon-top-disabled (register-icon "clojureacademy:node_top_0")
          icon-top-enabled (register-icon "clojureacademy:node_top_1")]
      (reset! icons icon-map)
      (reset! icons [:icon-top-disabled icon-top-disabled])
      (reset! icons [:icon-top-enabled icon-top-enabled])))
  (defn get-node-block-icon
    ([side metadata]
     (get @icons (if (or (= side 0) (= side 1)) :icon-top-enabled 1)))
    ([^IBlockAccess world x y z side]
     (let [tile-entity (get-tile-entity-at world x y z)
           tile-entity-right? (instance? tileentity-node tile-entity)
           enable (if tile-entity-right? (:enabled tile-entity) false)
           pct (if tile-entity-right? (min 4 (Math/round (double (/ (* 4 (:energy tile-entity)) (get-entity-max-energy tile-entity))))) 0)]                ;Math.min(4, Math.round((4 * node.getEnergy() / node.getMaxEnergy())));
       (if (or (== side 0) (== side 1))
         (if enable (:icon-top-enabled @icons) (:icon-top-disabled @icons))
         (get @icons pct))))))







(defmacro defblocknode [block-name type & args]
  (let [classdata (apply hash-map args)
        icons (atom {})
        classdata (assoc classdata :block-name (str "ac_node_" type))
        classdata (assoc classdata :container? true)
        classdata (assoc classdata :hardness (float 2.5))
        classdata (assoc classdata :harvest-level ["pickaxe", 1])
        classdata (assoc classdata :creative-tab `tab-clojure-academy)
        classdata (assoc-in classdata [:override :register-block-icons] `(fn [^IIconRegister ~'reg] (register-node-block-icons ~'reg ~type)))
        ;classdata (assoc-in classdata [:override :register-block-icons] `register-node-block-icons)
        classdata (assoc-in classdata [:override :get-icon] `get-node-block-icon)
        classdata (assoc-in classdata [:override :get-render-block-pass] `(constantly -1))
        classdata (assoc-in classdata [:override :is-opaque-cube] `(constantly false))
        classdata (assoc-in classdata [:override :on-block-placed] `(node-type->id type))
        classdata (assoc-in classdata [:override :create-new-tile-entity] `new-node-entity)
        classdata (reduce concat [] (into [] classdata))]
    `(defACblockcontainer ~block-name "node" ~@classdata)))

(defblocknode block-node-basic "basic")
(defblocknode block-node-standard "standard")
(defblocknode block-node-advanced "advanced")



