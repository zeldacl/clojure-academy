(ns cn.academy.block.matrix-registry
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-config :as config]
            [cn.academy.block.matrix-state :as state]
            [cn.academy.block.matrix-network :as network]
            [cn.academy.block.matrix-events :as events]
            [cn.academy.block.matrix-container :as container]
            [cn.academy.block.matrix-gui :as gui]))

(defprotocol IMatrixRegistry
  (create-matrix [this])
  (create-components [this])
  (register-all [this]))

(defrecord MatrixRegistryImpl [config resource-manager]
  IMatrixRegistry
  (create-matrix [_]
    (matrix/create-matrix))
  
  (create-components [this]
    (let [matrix (create-matrix this)
          state (state/create-matrix-state matrix config)
          network (network/create-matrix-network matrix)
          events (events/create-event-dispatcher matrix state network)
          container (container/create-matrix-container matrix)
          gui-handler (gui/create-matrix-gui matrix resource-manager)]
      {:matrix matrix
       :state state
       :network network
       :events events
       :container container
       :gui gui-handler}))
  
  (register-all [this]
    (let [components (create-components this)]
      {:matrix (:matrix components)
       :components components})))

(defn create-registry [config resource-manager]
  (->MatrixRegistryImpl config resource-manager))