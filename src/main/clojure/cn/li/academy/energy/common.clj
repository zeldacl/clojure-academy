(ns cn.li.academy.energy.common
  (:require [cn.li.mcmod.blocks :refer [defblockstate]]))

(defblockstate node-type [:integer 0 2])
(defblockstate connected [:bool])
(defblockstate energy [:integer 0 4])

(def config-node {
                  :basic    {:max-energy 15000 :band-width 150 :range 9 :capacity 5}
                  :standard {:max-energy 50000 :band-width 300 :range 12 :capacity 10}
                  :advanced {:max-energy 200000 :band-width 900 :range 19 :capacity 20}
                  })

(defn node-type-id->keyword [type-id]
  (condp = type-id
    0 :basic
    1 :standard
    2 :advanced
    :basic))

(defn get-node-attr [node-type-id attr]
  (get-in config-node [(node-type-id->keyword node-type-id) attr]))

