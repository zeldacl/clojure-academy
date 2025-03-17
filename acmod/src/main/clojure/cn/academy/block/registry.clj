(ns cn.academy.block.registry
  (:require [cn.academy.block.node-types :as types]))

(defprotocol IBlockRegistration
  (register-block! [this block-id block])
  (register-tile-entity! [this block tile-type]))

(defmulti create-node-block 
  "Create a node block implementation specific to Minecraft/Forge version"
  (fn [forge-version block-type block-properties] forge-version))

(defn register-node-blocks! [registry forge-version block-properties]
  (doseq [[type-key _] types/node-types]
    (let [block-id (str "node_" (name type-key))
          block (create-node-block forge-version type-key block-properties)]
      (register-block! registry block-id block))))