(ns cn.academy.block.matrix-serialization
  (:require [cn.academy.block.matrix-state :as state]
            [cn.academy.block.matrix-inventory :as inv]
            [cn.academy.block.matrix-energy :as energy]))

(defprotocol IMatrixSerializer
  "Protocol for matrix data serialization"
  (serialize-matrix [this matrix]
    "Serialize matrix to data format")
  (deserialize-matrix! [this matrix data]
    "Deserialize matrix from data format")
  (create-data-container [this]
    "Create empty data container"))

(defrecord MatrixSerializer []
  IMatrixSerializer
  (serialize-matrix [_ matrix]
    (let [container (create-data-container _)]
      (-> container
          (assoc :state (state/get-state-data (:state matrix)))
          (assoc :inventory (inv/get-inventory-data (:inventory matrix)))
          (assoc :energy {:stored (energy/get-energy-stored (:energy matrix))
                         :nodes (mapv serialize-node 
                                    (energy/get-connected-nodes (:energy matrix)))}))))
  
  (deserialize-matrix! [_ matrix data]
    (when data
      (state/load-state-data! (:state matrix) (:state data))
      (inv/load-inventory-data! (:inventory matrix) (:inventory data))
      (when-let [energy-data (:energy data)]
        (doseq [node (:nodes energy-data)]
          (energy/connect-node (:energy matrix) 
                             (deserialize-node node))))))
  
  (create-data-container [_]
    {}))

(defn- serialize-node [node]
  {:pos (:position node)
   :type (:type node)})

(defn- deserialize-node [data]
  {:position (:pos data)
   :type (:type data)})

(defn create-serializer []
  (->MatrixSerializer))