(ns cn.academy.block.matrix-registry
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-config :as config]
            [cn.academy.block.matrix-state :as state]
            [cn.academy.block.matrix-network :as network]
            [cn.academy.block.matrix-events :as events]
            [cn.academy.block.matrix-container :as container]
            [cn.academy.block.matrix-gui :as gui]))

(defprotocol IMatrixRegistry
  "Core protocol for matrix creation and component management"
  (create-matrix [this])
  (create-components [this])
  (register-all [this]))

(defprotocol IMatrixRegistration
  "Protocol for platform-specific registration"
  (register-block [this registry-handler])
  (register-tile-entity [this block-data registry-handler])
  (register-client-components [this client-handler]))

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
       :components components}))

  IMatrixRegistration
  (register-block [_ registry-handler]
    (let [matrix-data {:block-id "wireless_matrix"
                      :properties {:material :iron
                                 :hardness 3.0
                                 :resistance 15.0
                                 :light-level 0
                                 :has-tile-entity true}}]
      (.register-block! registry-handler matrix-data)))
  
  (register-tile-entity [_ block-data registry-handler]
    (let [tile-data {:block block-data
                     :tile-id "wireless_matrix"
                     :nbt-handler matrix/create-nbt-handler
                     :renderer matrix/create-renderer}]
      (.register-tile-entity! registry-handler tile-data)))
  
  (register-client-components [_ client-handler]
    (let [client-data {:model-id "wireless_matrix"
                      :model-loader matrix/create-model-loader
                      :renderer matrix/create-renderer}]
      (.register-client-components! client-handler client-data))))

(defn create-registry [config resource-manager]
  (->MatrixRegistryImpl config resource-manager))