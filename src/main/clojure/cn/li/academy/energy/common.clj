(ns cn.li.academy.energy.common
  (:require [cn.li.mcmod.blocks :refer [defblockstate]]))

(defblockstate node-type [:integer 0 2])
(defblockstate connected [:bool])
(defblockstate energy [:integer 0 4])

(defn get-max-energy [node-type]
  )
