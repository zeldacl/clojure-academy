(ns cn.academy.block.node
  (:require [cn.academy.block.node-types :as types]))

(defprotocol INodeBase
  "Core node functionality shared across implementations"
  (get-node-type [this])
  (get-energy [this])
  (get-max-energy [this])
  (get-bandwidth [this])
  (get-range [this])
  (get-capacity [this]))

(defprotocol INodeState
  "Node state management"
  (is-enabled? [this])
  (enable! [this])
  (disable! [this])
  (set-placer! [this player-id]))

(defrecord NodeState [node-type energy enabled placer-id]
  INodeBase
  (get-node-type [this] node-type)
  (get-energy [this] energy)
  (get-max-energy [this] (types/get-max-energy node-type))
  (get-bandwidth [this] (-> node-type types/get-node-type :bandwidth))
  (get-range [this] (-> node-type types/get-node-type :range))
  (get-capacity [this] (-> node-type types/get-node-type :capacity))

  INodeState
  (is-enabled? [this] enabled)
  (enable! [this] (assoc this :enabled true))
  (disable! [this] (assoc this :enabled false))
  (set-placer! [this player-id] (assoc this :placer-id player-id)))

(defn create-node-state [node-type]
  (->NodeState node-type 0 false nil))