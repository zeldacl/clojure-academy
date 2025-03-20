(ns cn.academy.core.node-types)

;; Common node type definitions with all properties
(def node-types
  {:basic {:name "basic"
           :max-energy 15000
           :bandwidth 150
           :range 9
           :capacity 5
           :render-type :model}
   :standard {:name "standard"
              :max-energy 50000
              :bandwidth 300
              :range 12
              :capacity 10
              :render-type :model}
   :advanced {:name "advanced"
              :max-energy 200000
              :bandwidth 900
              :range 19
              :capacity 20
              :render-type :model}})

;; Helper functions to access node properties
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