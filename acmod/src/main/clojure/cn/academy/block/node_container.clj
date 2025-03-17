(ns cn.academy.block.node-container
  (:require [cn.academy.block.node :as node]))

(defprotocol IContainer
  (is-valid-slot? [this slot])
  (can-interact? [this player])
  (get-stack-limit [this slot])
  (on-content-changed [this]))

(defrecord NodeInventory [node-state items]
  IContainer
  (is-valid-slot? [_ slot]
    (< slot (node/get-capacity node-state)))
  
  (can-interact? [_ player]
    true)
  
  (get-stack-limit [_ slot]
    64)
  
  (on-content-changed [_]
    ; Update node state when inventory changes
    nil))

(defn create-inventory [node-state]
  (->NodeInventory node-state {}))