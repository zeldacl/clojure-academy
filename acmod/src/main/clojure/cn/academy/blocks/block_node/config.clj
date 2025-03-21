(ns cn.academy.blocks.block-node.config)

(def node-types
  {:basic {:max-energy 5000
           :range 8
           :max-connections 4
           :bandwidth 500}
   
   :standard {:max-energy 20000
              :range 16
              :max-connections 8
              :bandwidth 1000}
   
   :advanced {:max-energy 100000
              :range 32
              :max-connections 16
              :bandwidth 2000}})

(defn get-node-config [node-type]
  (get node-types node-type))

(defn get-node-property [node-type property]
  (get-in node-types [node-type property]))