(ns cn.academy.block.block.block-cat-engine
  (:require [cn.academy.api.block :as block-api]
            [cn.academy.energy.api.wireless-helper :as wireless]
            [cn.academy.energy.api.block.wireless-node :as wireless-node]
            [cn.lambdalib2.util.rand-utils :as rand-utils])
  (:import [net.minecraft.block.material Material]))

(defprotocol ICatEngineBlock
  (create-tile-entity [this world meta])
  (on-block-activated [this world pos state player hand facing hit-x hit-y hit-z])
  (is-opaque-cube? [this state])
  (get-render-type [this state]))

(defrecord CatEngineBlock [properties]
  ICatEngineBlock
  (create-tile-entity [_ world meta]
    (block-api/create-tile-entity 
      (merge 
        {:block-type :cat-engine}
        properties)))
  
  (on-block-activated [_ world pos state player hand facing hit-x hit-y hit-z]
    (when-let [tile-entity (block-api/get-tile-entity world pos)]
      (when (block-api/is-cat-engine? tile-entity)
        (when-not (block-api/is-remote? world)
          (if (wireless/generator-linked? tile-entity)
            (do 
              (wireless/unlink-generator! tile-entity)
              (block-api/send-message player "ac.cat_engine.unlink"))
            (let [nodes (wireless/get-nodes-in-range world pos)]
              (if (empty? nodes)
                (block-api/send-message player "ac.cat_engine.notfound")
                (let [node (rand-utils/rand-nth nodes)]
                  (block-api/send-message player "ac.cat_engine.linked" [(wireless-node/get-node-name node)])
                  (wireless/link-generator! tile-entity node))))))
        true)))
  
  (is-opaque-cube? [_ _]
    false)
  
  (get-render-type [_ _]
    :invisible))

(defn create-cat-engine []
  (->CatEngineBlock
    {:material Material/ROCK}))