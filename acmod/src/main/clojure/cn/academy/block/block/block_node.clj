(ns cn.academy.block.block.block-node
  (:require [cn.academy.block.node :as node]
            [cn.academy.api.block :as block-api])
  (:import [net.minecraft.block.material Material]))

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

(defprotocol INodeBlock
  (create-tile-entity [this world meta])
  (get-node-properties [this])
  (get-container [this player world pos]))

(defrecord NodeBlock [properties type]
  INodeBlock
  (create-tile-entity [_ world meta]
    (block-api/create-tile-entity 
      (merge 
        {:node-type type
         :max-energy (get-in node-types [type :max-energy])
         :bandwidth (get-in node-types [type :bandwidth])
         :range (get-in node-types [type :range])
         :capacity (get-in node-types [type :capacity])}
        properties)))
  
  (get-node-properties [_]
    (get node-types type))
  
  (get-container [_ player world pos]
    (when-let [tile (.getTileEntity world pos)]
      (when (instance? cn.academy.block.tileentity.TileNode tile)
        (block-api/create-container {:tile tile :player player})))))

(defn create-node-block [type]
  (->NodeBlock 
    {:material Material/ROCK
     :hardness 2.5
     :harvest-level ["pickaxe" 1]}
    type))