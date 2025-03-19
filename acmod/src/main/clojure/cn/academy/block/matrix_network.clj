(ns cn.academy.block.matrix-network
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IMatrixNetwork
  (sync-plate-count [this])
  (sync-placer-name [this])
  (sync-capabilities [this])
  (register-listeners [this])
  (connect-to-network [this network-id password])
  (leave-network [this])
  (get-network-id [this])
  (sync-network-state [this])
  (handle-network-sync [this state]))

(defrecord MatrixNetworkHandler [matrix network-state]
  IMatrixNetwork
  (sync-plate-count [_]
    {:type :sync-plate-count
     :count (matrix/get-plate-count matrix)})
  
  (sync-placer-name [_]
    {:type :sync-placer-name
     :name (matrix/get-placer-name matrix)})
  
  (sync-capabilities [_]
    {:type :sync-capabilities
     :capacity (matrix/get-capacity matrix)
     :bandwidth (matrix/get-bandwidth matrix)
     :range (matrix/get-range matrix)})
  
  (register-listeners [_]
    {:channels ["matrix.sync" "matrix.update"]
     :handlers {:sync #(handle-sync matrix %)
               :update #(handle-update matrix %)}})
  
  (connect-to-network [_ network-id password]
    (when (matrix/is-valid? matrix)
      (swap! network-state assoc 
             :id network-id 
             :password password)
      true))
  
  (leave-network [_]
    (reset! network-state nil)
    true)
  
  (get-network-id [_]
    (:id @network-state))
  
  (sync-network-state [_]
    {:type :network-sync
     :data @network-state})
  
  (handle-network-sync [_ state]
    (reset! network-state state)))

(defn- handle-sync [matrix msg]
  (case (:type msg)
    :sync-plate-count (swap! (:render-state matrix) assoc :plate-count (:count msg))
    :sync-placer-name (matrix/set-placer! matrix (:name msg))
    :sync-capabilities (swap! (:state matrix) merge (select-keys msg [:capacity :bandwidth :range]))))

(defn- handle-update [matrix msg]
  (matrix/update! matrix))

(defn create-network-handler [matrix]
  (->MatrixNetworkHandler matrix (atom nil)))