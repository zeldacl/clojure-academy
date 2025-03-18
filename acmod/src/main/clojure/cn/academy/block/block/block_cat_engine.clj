(ns cn.academy.block.block.block-cat-engine
  (:require [cn.academy.api.block :as block-api]
            [cn.academy.energy.api.wireless :as wireless]
            [cn.academy.energy.api.wireless-helper :as wireless-helper]
            [cn.academy.block.tileentity.tile-cat-engine :as tile-engine]
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
          (if (wireless-helper/is-generator-linked? tile-entity)
            (do 
              (wireless-helper/unlink-generator! tile-entity)
              (block-api/send-message player "ac.cat_engine.unlink"))
            (let [nodes (wireless-helper/get-nodes-in-range world pos)]
              (if (empty? nodes)
                (block-api/send-message player "ac.cat_engine.notfound")
                (let [node (rand-nth nodes)]
                  (wireless-helper/link-generator! tile-entity node)
                  (block-api/send-message player "ac.cat_engine.linked" 
                                        [(wireless/get-network-name node)]))))))
        true)))
  
  (is-opaque-cube? [_ _]
    false)
  
  (get-render-type [_ _]
    :invisible))

(defn create []
  (->CatEngineBlock
    {:material Material/ROCK}))