(ns cn.academy.block.registry
  (:require [cn.academy.block.node-types :as types]
            [cn.academy.block.block.block-matrix :as matrix]
            [mcmod.protocols :refer :all])
  (:import [net.minecraft.block.material Material]))

(def registered-blocks (atom {}))
(def registered-items (atom {}))
(def registered-tile-entities (atom {}))

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

(defn register-matrix! [registry]
  (let [matrix-def (matrix/create-matrix)]
    (register-block! registry "matrix" matrix-def)))

(defn create-block [id properties]
  (let [block (reify IBlock
                (get-properties [_] properties)
                (get-material [_] (:material properties Material/ROCK))
                (get-hardness [_] (:hardness properties 3.0))
                (get-resistance [_] (:resistance properties 3.0))
                (get-light-level [_] (:light-level properties 0))
                (on-activated [_ pos data] 
                  (when-let [handler (:on-activated properties)]
                    (handler pos data)))
                (on-placed [_ pos data]
                  (when-let [handler (:on-placed properties)]
                    (handler pos data)))
                (on-removed [_ pos]
                  (when-let [handler (:on-removed properties)]
                    (handler pos))))]
    (swap! registered-blocks assoc id block)
    block))

(defn register-tile-entity! [block-id te-type tile-entity]
  (swap! registered-tile-entities assoc block-id [te-type tile-entity]))

(defn get-registered-blocks []
  @registered-blocks)

(defn get-registered-tile-entities []
  @registered-tile-entities)

(defn get-registered-items []
  @registered-items)