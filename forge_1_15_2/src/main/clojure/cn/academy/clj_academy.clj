(ns cn.academy.clj-academy
  (:require [cn.academy.block.block.block-node :as block-node])
  (:import [net.minecraft.creativetab CreativeTabs]))

(def block-node-instance
  (doto (block-node/block-factory-createBasic)
    (.setCreativeTab CreativeTabs/BUILDING_BLOCKS)))