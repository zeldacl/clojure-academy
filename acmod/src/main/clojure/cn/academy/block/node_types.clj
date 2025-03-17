(ns cn.academy.block.node-types)

(def node-types
  {:basic {:name "basic"
           :max-energy 15000
           :bandwidth 150
           :range 9
           :capacity 5}
   :standard {:name "standard"
              :max-energy 50000
              :bandwidth 300
              :range 12
              :capacity 10}
   :advanced {:name "advanced"
              :max-energy 200000
              :bandwidth 900
              :range 19
              :capacity 20}})

(defn get-node-type [type-key]
  (get node-types type-key))

(defn get-type-name [type-key]
  (:name (get-node-type type-key)))

(defn get-max-energy [type-key]
  (:max-energy (get-node-type type-key)))

(defn get-bandwidth [type-key]
  (:bandwidth (get-node-type type-key)))