(ns cn.academy.core.node-types
  "Common definitions for node types across the codebase")

;; Node type definitions with their properties
(def node-types
  {:basic    {:max-energy 15000
              :bandwidth  150
              :range      9
              :capacity   5
              :render-type :normal}
   :standard {:max-energy 50000
              :bandwidth  300
              :range      12
              :capacity   10
              :render-type :normal}
   :advanced {:max-energy 200000
              :bandwidth  900
              :range      19
              :capacity   20
              :render-type :special}})

;; Helper functions for accessing node properties
(defn get-node-max-energy [node-type]
  (get-in node-types [node-type :max-energy]))

(defn get-node-bandwidth [node-type]
  (get-in node-types [node-type :bandwidth]))

(defn get-node-range [node-type]
  (get-in node-types [node-type :range]))

(defn get-node-capacity [node-type]
  (get-in node-types [node-type :capacity]))

(defn get-node-render-type [node-type]
  (get-in node-types [node-type :render-type]))

;; Common protocol for wireless nodes
(defprotocol IWirelessNode
  "Protocol for wireless node functionality"
  (get-node-type [this] "Get the node type")
  (get-energy [this] "Get current energy")
  (get-max-energy [this] "Get maximum energy capacity")
  (get-bandwidth [this] "Get energy transfer rate")
  (get-range [this] "Get wireless range")
  (get-capacity [this] "Get connection capacity")
  (set-placer [this player] "Set the player who placed the node"))

;; Reusable implementation for node state with energy functions
(defn charge-node
  "Generic charge function that respects bandwidth limits"
  [state-atom get-max-fn get-bandwidth-fn amount ignore-bandwidth?]
  (let [state @state-atom
        bandwidth (if ignore-bandwidth? amount (get-bandwidth-fn state))
        max-charge (min amount bandwidth)
        current (:energy state)
        max-energy (get-max-fn state)
        actual-charge (min max-charge (- max-energy current))]
    (swap! state-atom update :energy #(+ % actual-charge))
    actual-charge))

(defn discharge-node
  "Generic discharge function that respects bandwidth limits"
  [state-atom get-bandwidth-fn amount ignore-bandwidth?]
  (let [state @state-atom
        bandwidth (if ignore-bandwidth? amount (get-bandwidth-fn state))
        max-discharge (min amount bandwidth)
        current (:energy state)
        actual-discharge (min max-discharge current)]
    (swap! state-atom update :energy #(- % actual-discharge))
    actual-discharge))