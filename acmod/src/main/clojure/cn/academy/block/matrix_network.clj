(ns cn.academy.block.matrix-network
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IMatrixNetwork
  (sync-plate-count [this])
  (sync-placer-name [this])
  (sync-capabilities [this])
  (register-listeners [this]))

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
               :update #(handle-update matrix %)}}))

(defn- handle-sync [matrix msg]
  (case (:type msg)
    :sync-plate-count (swap! (:render-state matrix) assoc :plate-count (:count msg))
    :sync-placer-name (matrix/set-placer! matrix (:name msg))
    :sync-capabilities (swap! (:state matrix) merge (select-keys msg [:capacity :bandwidth :range]))))

(defn- handle-update [matrix msg]
  (matrix/update! matrix))

(defn create-network-handler [matrix]
  (->MatrixNetworkHandler matrix (atom {})))