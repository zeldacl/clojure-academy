(ns cn.academy.block.registry-def
  (:require [cn.academy.block.block.block-matrix :as matrix]
            [cn.academy.block.block.block-cat-engine :as cat-engine]
            [cn.academy.block.block.block-node :as node]
            [mcmod.protocols :refer :all]
            [cn.academy.block.block-node :as block-node]
            [cn.academy.block.tileentity.node-tile :as node-tile]))

;; Registry entries that will be used by all forge versions
(def registry-entries
  {:blocks
   {"matrix" {:def matrix/block-matrix-def
              :register matrix/register!}
    "cat_engine" {:def cat-engine/block-def
                  :register cat-engine/register!}
    "node_basic" {:def (node/create-node-def :basic)
                  :register node/register!}
    "node_standard" {:def (node/create-node-def :standard)
                    :register node/register!}
    "node_advanced" {:def (node/create-node-def :advanced)
                    :register node/register!}}})

;; Block registration definitions
(defn register-blocks! [registry mod-id]
  ;; Register the three types of wireless nodes
  (let [register-fn #(register-block! registry %1 %2)]
    
    ;; Basic Node
    (register-fn 
      "node_basic" 
      (block-node/create-basic-node))
    
    ;; Standard Node  
    (register-fn
      "node_standard"
      (block-node/create-standard-node))
    
    ;; Advanced Node
    (register-fn
      "node_advanced" 
      (block-node/create-advanced-node)))
  
  ;; Add other block registrations here
  )

;; Tile entity registration
(defn register-tile-entities! [registry mod-id]
  ;; Register tile entities for each node type
  (register-tile-entity! registry 
                        "node_basic" 
                        #(node-tile/create-node-tile :basic))
  
  (register-tile-entity! registry 
                        "node_standard" 
                        #(node-tile/create-node-tile :standard))
  
  (register-tile-entity! registry 
                        "node_advanced" 
                        #(node-tile/create-node-tile :advanced))
  
  ;; Add other tile entity registrations here
  )