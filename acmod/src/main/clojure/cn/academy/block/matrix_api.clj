(ns cn.academy.block.matrix-api
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IMatrixAPI
  (get-capacity-at [this pos])
  (get-range-at [this pos])
  (can-connect? [this source-pos target-pos])
  (create-network [this pos password])
  (connect-to-network [this pos network-id password]))

(defprotocol IMatrixNetwork
  (add-node [this pos])
  (remove-node [this pos])
  (get-connected-nodes [this])
  (get-network-load [this]))

(defrecord MatrixAPI [matrix-registry]
  IMatrixAPI
  (get-capacity-at [_ pos]
    (when-let [matrix (get-matrix-at pos)]
      (matrix/get-capacity matrix)))
  
  (get-range-at [_ pos]
    (when-let [matrix (get-matrix-at pos)]
      (matrix/get-range matrix)))
  
  (can-connect? [_ source-pos target-pos]
    (when-let [source (get-matrix-at source-pos)]
      (and (matrix/working? source)
           (<= (calc-distance source-pos target-pos)
               (matrix/get-range source)))))
  
  (create-network [_ pos password]
    (when-let [matrix (get-matrix-at pos)]
      (create-matrix-network matrix password)))
  
  (connect-to-network [_ pos network-id password]
    (when-let [matrix (get-matrix-at pos)]
      (connect-matrix-to-network matrix network-id password))))

(defn- get-matrix-at [pos]
  (when-let [tile (get-tile-at pos)]
    (when (instance? cn.academy.block.matrix.MatrixCore tile)
      tile)))

(defn- calc-distance [pos1 pos2]
  (Math/sqrt (+ (Math/pow (- (:x pos2) (:x pos1)) 2)
                (Math/pow (- (:y pos2) (:y pos1)) 2)
                (Math/pow (- (:z pos2) (:z pos1)) 2))))