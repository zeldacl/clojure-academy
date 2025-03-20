(ns cn.academy.block.matrix.container
  (:require [cn.academy.block.matrix :as matrix])
  (:import [net.minecraft.entity.player EntityPlayer]))

(defprotocol IMatrixContainer
  (can-interact-with? [this player])
  (get-energy-info [this])
  (transfer-energy [this amount simulate?])
  (get-core-slot [this])
  (get-plate-slots [this])
  (handle-slot-click [this slot-id button]))

(defrecord MatrixContainer [matrix player inventory]
  IMatrixContainer
  (can-interact-with? [_ player]
    (let [pos (matrix/get-position matrix)]
      (.withinDistance player 
                      (:x pos) 
                      (:y pos) 
                      (:z pos) 
                      64.0)))
  
  (get-energy-info [_]
    {:current (matrix/get-energy-stored matrix)
     :max (matrix/get-energy-capacity matrix)})
  
  (transfer-energy [_ amount simulate?]
    (matrix/receive-energy matrix amount simulate?))
  
  (get-core-slot [_]
    {:id 0
     :type :core
     :item (matrix/get-core-item matrix)})
  
  (get-plate-slots [_]
    (map-indexed 
     (fn [idx _]
       {:id (inc idx)
        :type :plate
        :item (matrix/get-plate-item matrix idx)})
     (range 3)))
  
  (handle-slot-click [_ slot-id button]
    (case slot-id
      0 (matrix/handle-core-slot matrix button)
      (1 2 3) (matrix/handle-plate-slot matrix (dec slot-id) button)
      nil)))

(defn create-container
  "Create new matrix container instance"
  [matrix player]
  (->MatrixContainer matrix 
                     player
                     (atom {:slots {}})))