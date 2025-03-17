(ns cn.academy.block.matrix-events
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-state :as state]
            [cn.academy.block.matrix-network-cap :as network]))

(defprotocol IMatrixEventHandler
  (on-block-place [this world pos placer])
  (on-block-break [this world pos])
  (on-block-activate [this world pos player hand facing hit])
  (on-network-join [this world pos network-id password])
  (on-network-leave [this world pos]))

(defrecord MatrixEventDispatcher [matrix state network-handler]
  IMatrixEventHandler
  (on-block-place [_ world pos placer]
    (when (instance? matrix/IMatrix placer)
      (let [tile (matrix/get-matrix-at world pos)]
        (matrix/set-placer! tile (.getName placer))
        (state/init-state! state))))
  
  (on-block-break [_ world pos]
    (when-let [tile (matrix/get-matrix-at world pos)]
      (network/leave-network network-handler)))
  
  (on-block-activate [_ world pos player hand facing hit]
    (when-not (.isSneaking player)
      (when-let [tile (matrix/get-matrix-at world pos)]
        {:type :open-gui
         :gui-id :matrix
         :pos pos})))
  
  (on-network-join [_ world pos network-id password]
    (when-let [tile (matrix/get-matrix-at world pos)]
      (network/join-network network-handler network-id password)))
  
  (on-network-leave [_ world pos]
    (when-let [tile (matrix/get-matrix-at world pos)]
      (network/leave-network network-handler))))

(defn create-event-dispatcher [matrix state network-handler]
  (->MatrixEventDispatcher matrix state network-handler))