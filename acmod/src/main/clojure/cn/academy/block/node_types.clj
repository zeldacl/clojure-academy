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

(defn get-property [type-key property]
  (get (get-node-type type-key) property))

(defn get-type-name [type-key]
  (get-property type-key :name))

(defn get-max-energy [type-key]
  (get-property type-key :max-energy))

(defn get-bandwidth [type-key]
  (get-property type-key :bandwidth))

(defn get-range [type-key]
  (get-property type-key :range))

(defn get-capacity [type-key]
  (get-property type-key :capacity))