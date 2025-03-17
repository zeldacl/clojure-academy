(ns cn.academy.block.matrix-core)

(defprotocol IMatrixBlock
  (get-hardness [this])
  (get-light-level [this])
  (get-rot-center [this])
  (get-sub-blocks [this]))

(defrecord MatrixBlockCore []
  IMatrixBlock
  (get-hardness [_] 3.0)
  (get-light-level [_] 1.0)
  (get-rot-center [_] [1.0 0.0 1.0])
  (get-sub-blocks [_]
    [[0 0 1]
     [1 0 1]
     [1 0 0]
     [0 1 0]
     [0 1 1]
     [1 1 1]
     [1 1 0]]))

(defprotocol IMatrixTile
  (get-placer [this])
  (set-placer! [this player]))

(defrecord MatrixTileCore [placer]
  IMatrixTile
  (get-placer [this] @(:placer this))
  (set-placer! [this player] 
    (reset! (:placer this) player)))

(defn create-matrix-block []
  (->MatrixBlockCore))

(defn create-matrix-tile []
  (->MatrixTileCore (atom nil)))