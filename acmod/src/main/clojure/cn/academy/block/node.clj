(ns cn.academy.block.node
  (:require [cn.academy.block.node-types :as types]))

(defprotocol INodeBlock
  (get-energy [this])
  (get-max-energy [this])
  (is-connected [this])
  (set-placer! [this player-id])
  (get-node-type [this]))

(defprotocol INodeTile
  (get-capacity [this])
  (get-bandwidth [this])
  (get-range [this])
  (enable! [this])
  (disable! [this]))

(defrecord NodeState [node-type energy enabled placer-id]
  INodeBlock
  (get-energy [this] energy)
  (get-max-energy [this] (types/get-max-energy node-type))
  (is-connected [this] enabled)
  (set-placer! [this player-id] 
    (assoc this :placer-id player-id))
  (get-node-type [this] node-type)

  INodeTile
  (get-capacity [this] (-> node-type types/get-node-type :capacity))
  (get-bandwidth [this] (-> node-type types/get-node-type :bandwidth))
  (get-range [this] (-> node-type types/get-node-type :range))
  (enable! [this] (assoc this :enabled true))
  (disable! [this] (assoc this :enabled false)))

(defn create-node-state [node-type]
  (->NodeState node-type 0 false nil))