(ns cn.li.academy.ac-blocks
  (:require [cn.li.mcmod.blocks :refer [instance-block]]
            [cn.li.academy.energy.blocks.node :refer [block-node]]
            [cn.li.mcmod.registry :refer [registry-block]]))


(def block-node-instance (instance-block block-node))
(registry-block block-node-instance)

