(ns cn.academy.blocks.block_matrix.registry
  (:require [cn.academy.blocks.block_matrix :as matrix]
            [cn.academy.blocks.block_matrix.state :as state]
            [cn.academy.blocks.block_matrix.network :as network]
            [cn.academy.blocks.block_matrix.energy :as energy]
            [cn.academy.blocks.block_matrix.gui :as gui]
            [cn.academy.blocks.block_matrix.render :as render]))

(def matrix-block-def
  {:id "wireless_matrix"
   :properties {:material :iron
               :hardness 3.0
               :resistance 15.0
               :light-level 0
               :has-tile-entity true}})

(defprotocol IMatrixRegistry
  "Protocol for managing matrix registration"
  (register-matrix-block [this])
  (register-tile-entity [this])
  (register-renderer [this])
  (register-container [this])
  (register-all [this]))

(defrecord MatrixRegistry [mod-id config]
  IMatrixRegistry
  (register-matrix-block [_]
    (merge matrix-block-def
           {:mod-id mod-id}))
  
  (register-tile-entity [_]
    {:block-id (:id matrix-block-def)
     :factory (fn [world pos]
                (let [matrix (matrix/create-matrix :position pos)
                      state (state/create-matrix-state matrix config)
                      network (network/create-network matrix)
                      energy (energy/create-energy matrix state)]
                  {:matrix matrix
                   :state state
                   :network network
                   :energy energy}))})
  
  (register-renderer [_]
    {:block-id (:id matrix-block-def)
     :factory (fn [matrix]
                (render/create-renderer matrix))})
  
  (register-container [_]
    {:block-id (:id matrix-block-def)
     :factory (fn [matrix player]
                (gui/create-gui matrix))})
  
  (register-all [this]
    {:block (register-matrix-block this)
     :tile-entity (register-tile-entity this)
     :renderer (register-renderer this)
     :container (register-container this)}))

(defn create-registry [mod-id config]
  (->MatrixRegistry mod-id config))