(ns cn.academy.block.matrix.network)

(defprotocol IMatrixNetwork
  (connect-to-network [this network-id password])
  (leave-network [this])
  (get-network-id [this])
  (get-bandwidth [this])
  (get-connected-nodes [this])
  (sync-network-state [this])
  (handle-network-sync [this state]))

(defrecord MatrixNetwork [matrix network-state]
  IMatrixNetwork
  (connect-to-network [_ network-id password]
    (swap! network-state assoc 
           :id network-id 
           :password password
           :connected true)
    true)
  
  (leave-network [_]
    (reset! network-state {:connected false})
    true)
  
  (get-network-id [_]
    (:id @network-state))
  
  (get-bandwidth [_]
    (:bandwidth @network-state))
  
  (get-connected-nodes [_]
    (:nodes @network-state))
  
  (sync-network-state [_]
    {:type :network-sync
     :data @network-state})
  
  (handle-network-sync [_ state]
    (swap! network-state merge state)))

(defn create-network 
  "Create new matrix network instance"
  [matrix]
  (->MatrixNetwork matrix 
                   (atom {:connected false
                         :bandwidth 1000
                         :nodes #{}})))