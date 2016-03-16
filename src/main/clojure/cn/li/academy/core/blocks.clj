(ns cn.li.academy.core.blocks
  (:require [forge-clj.blocks :refer [defblock]]
            [cn.li.academy.tab :refer [tab-clojure-academy]]
            [forge-clj.util :refer [remote? get-tile-entity-at drop-items open-gui]])
  (:import (net.minecraft.inventory IInventory)
           (net.minecraft.block Block)))

(defmacro defACblock [name block-name & args]
  (let [classdata (apply hash-map args)
        classdata (assoc classdata :creative-tab (if (:creative-tab classdata) (:creative-tab classdata) `tab-clojure-academy))
        classdata (assoc classdata :block-texture-name (if (:block-texture-name classdata) (:block-texture-name classdata) (str "academy:" block-name)) )
        classdata (assoc classdata :block-name (if (:block-name classdata) (:block-name classdata) (str "ac_" block-name)))
        classdata (reduce concat [] (into [] classdata))]
    `(defblock ~name ~@classdata)))

(defn break-ac-block-container [world x y z block wtf this]
  (when (remote? world)
    (drop-items world x y z))
  (let [this ^Block this]
    (proxy-super breakBlock world x y z block wtf)))

(defn open-ac-inventory-gui [world x y z player _ _ _ _]
  (if (not (remote? world))
    true
    ;(open-gui player (deref mod-instance) 0 world x y z)
    )
  true)

(defmacro defACblockcontainer [name block-name & args]
  (let [classdata (apply hash-map args)
        classdata (assoc classdata :creative-tab (if (:creative-tab classdata) (:creative-tab classdata) `tab-clojure-academy))
        classdata (assoc classdata :block-texture-name (if (:block-texture-name classdata) (:block-texture-name classdata) (str "academy:" block-name)))
        classdata (assoc classdata :block-name (if (:block-name classdata) (:block-name classdata) (str "ac_" block-name)))
        classdata (assoc-in classdata [:override :break-block] `(fn [~'world ~'x ~'y ~'z ~'block ~'wtf] (break-ac-block-container ~'world ~'x ~'y ~'z ~'block ~'wtf ~'this)))
        classdata (assoc-in classdata [:override :on-block-activated] open-ac-inventory-gui)
        classdata (reduce concat [] (into [] classdata))]
    `(do
       (defblock ~name ~@classdata))))
