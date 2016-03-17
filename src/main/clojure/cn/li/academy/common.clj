(ns cn.li.academy.common
  (:require [cn.li.academy.energy.blocks :refer [block-node-basic block-node-standard block-node-advanced]]
            [forge-clj.registry :refer [register register-tile-entity register-events register-gui-handler]]
            [cn.li.academy.items :refer [logo]]))

(defn common-pre-init [this event])

(defn common-init [this event]
  (register logo "logo")
  (register block-node-basic "block-node-basic")
  (register block-node-standard "block-node-standard")
  (register block-node-advanced "block-node-advanced"))

(defn common-post-init [this event])

