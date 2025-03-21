(ns cn.academy.blocks.block_matrix.events
  (:require [cn.academy.blocks.block_matrix :as matrix]))

(defprotocol IMatrixEventHandler
  (on-block-place [this world pos placer])
  (on-block-break [this world pos])
  (on-block-activate [this world pos player hand facing hit])
  (on-network-join [this world pos network-id password])
  (on-network-leave [this world pos]))

(defrecord MatrixEventDispatcher [matrix state network]
  IMatrixEventHandler
  (on-block-place [_ world pos placer]
    (when placer
      (matrix/set-placer! matrix (.getName placer))))
  
  (on-block-break [_ world pos]
    (when (matrix/is-formed? matrix)
      (matrix/leave-network matrix)))
  
  (on-block-activate [_ world pos player hand facing hit]
    (when (and (not (.isSneaking player))
               (matrix/can-interact? matrix player))
      {:type :open-gui
       :gui-id :matrix
       :pos pos}))
  
  (on-network-join [_ world pos network-id password]
    (when (matrix/is-valid? matrix)
      (matrix/join-network matrix network-id password)))
  
  (on-network-leave [_ world pos]
    (matrix/leave-network matrix)))

(defn create-event-dispatcher [matrix state network]
  (->MatrixEventDispatcher matrix state network))