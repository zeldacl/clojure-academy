(ns cn.academy.block.matrix-tile
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IMatrixTileState
  "Protocol for managing tile entity state"
  (get-core-state [this] "Get core state info")
  (get-plate-state [this] "Get plate state info")
  (update-state! [this delta-time] "Update tile entity state")
  (serialize-state [this] "Serialize state to data")
  (deserialize-state! [this data] "Deserialize state from data"))

(defprotocol IMatrixTileCapability
  "Protocol for tile entity capabilities"
  (get-capabilities [this] "Get supported capabilities")
  (get-capability-handler [this capability side] "Get handler for capability")
  (invalidate-capabilities! [this] "Invalidate all capabilities"))

(defprotocol IMatrixTileNetwork
  "Protocol for tile entity networking"
  (get-network-id [this] "Get network ID")
  (join-network! [this network-id password] "Join network")
  (leave-network! [this] "Leave current network")
  (sync-with-client! [this] "Sync state with client"))

(defprotocol IMatrixTileRenderer
  "Protocol for tile entity rendering"
  (get-render-data [this] "Get rendering data")
  (should-render-off-screen? [this] "Check if should render when off screen"))

(defrecord MatrixTileEntity [matrix state-atom network-handler renderer-handler]
  IMatrixTileState
  (get-core-state [_]
    {:level (matrix/get-core-level matrix)
     :formed? (matrix/is-formed? matrix)})
  
  (get-plate-state [_]
    {:count (matrix/get-plate-count matrix)
     :positions (matrix/get-plate-positions matrix)})
  
  (update-state! [_ delta-time]
    (when (matrix/is-formed? matrix)
      (swap! state-atom update :time + delta-time)))
  
  (serialize-state [_]
    (merge @state-atom (matrix/save-to-nbt matrix)))
  
  (deserialize-state! [_ data]
    (reset! state-atom (dissoc data :matrix))
    (matrix/load-from-nbt! matrix (:matrix data)))

  IMatrixTileCapability
  (get-capabilities [_]
    #{:energy :inventory})
  
  (get-capability-handler [_ capability side]
    (when (contains? (get-capabilities _) capability)
      (case capability
        :energy (matrix/get-energy-handler matrix)
        :inventory nil)))
  
  (invalidate-capabilities! [_]
    (matrix/break! matrix))

  IMatrixTileNetwork
  (get-network-id [_]
    (:network-id @state-atom))
  
  (join-network! [_ network-id password]
    (.join-network network-handler network-id password))
  
  (leave-network! [_]
    (.leave-network network-handler))
  
  (sync-with-client! [_]
    (.sync-state network-handler (serialize-state _)))

  IMatrixTileRenderer
  (get-render-data [_]
    {:formed? (matrix/is-formed? matrix)
     :core-level (matrix/get-core-level matrix)
     :plate-count (matrix/get-plate-count matrix)
     :time (:time @state-atom)})
  
  (should-render-off-screen? [_]
    false))

(defn create-matrix-tile 
  "Create a new matrix tile entity instance"
  [matrix]
  (->MatrixTileEntity 
    matrix
    (atom {:time 0.0
           :network-id nil
           :last-sync 0})
    (matrix/create-network-handler matrix)
    (matrix/create-renderer-handler matrix)))