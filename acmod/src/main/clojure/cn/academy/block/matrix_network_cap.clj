(ns cn.academy.block.matrix-network-cap
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-api :as api]))

(defprotocol IMatrixNetworkCapability
  (get-network-id [this])
  (join-network [this network-id password])
  (leave-network [this])
  (is-connected? [this])
  (get-available-networks [this]))

(defrecord MatrixNetworkHandler [matrix network-state]
  IMatrixNetworkCapability
  (get-network-id [_]
    (:network-id @network-state))
  
  (join-network [_ network-id password]
    (when (matrix/working? matrix)
      (let [range (matrix/get-range matrix)]
        (when (can-join-network? network-id password range)
          (swap! network-state assoc 
                 :network-id network-id
                 :connected true)
          true))))
  
  (leave-network [_]
    (swap! network-state assoc 
           :network-id nil
           :connected false)
    true)
  
  (is-connected? [_]
    (boolean (:connected @network-state)))
  
  (get-available-networks [_]
    (when (matrix/working? matrix)
      (let [range (matrix/get-range matrix)
            pos (get-in @(:info matrix) [:pos])]
        (find-networks-in-range pos range)))))

(defn- can-join-network? [network-id password range]
  ;; Check if network exists and password matches
  (when-let [network (get-network network-id)]
    (and (valid-password? network password)
         (has-capacity? network))))

(defn- find-networks-in-range [pos range]
  ;; Find all matrix networks within range
  (->> (get-all-networks)
       (filter #(within-range? pos (:pos %) range))))