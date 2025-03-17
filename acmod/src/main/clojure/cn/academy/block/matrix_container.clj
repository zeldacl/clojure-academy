(ns cn.academy.block.matrix-container
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IMatrixContainer
  (init-slots [this])
  (can-interact? [this player])
  (get-slot-stack [this slot])
  (set-slot-stack [this slot stack])
  (transfer-stack [this from-slot to-slot max-size]))

(defrecord MatrixContainer [matrix player]
  IMatrixContainer
  (init-slots [_]
    [{:type :plate :x 78 :y 11}  ; Plate slot 0
     {:type :plate :x 53 :y 60}  ; Plate slot 1
     {:type :plate :x 104 :y 60} ; Plate slot 2
     {:type :core :x 78 :y 36}]) ; Core slot 3
  
  (can-interact? [_ player]
    true)  ; Could add permission checks here
  
  (get-slot-stack [_ slot]
    (get @(:inventory matrix) slot))
  
  (set-slot-stack [_ slot stack]
    (swap! (:inventory matrix) assoc slot stack))
  
  (transfer-stack [this from-slot to-slot max-size]
    (let [from-stack (get-slot-stack this from-slot)]
      (when (and from-stack 
                 (matrix/valid-slot? matrix to-slot from-stack))
        {:allow true
         :max-size (min max-size 
                       (if (<= 0 to-slot 3) 1 64))}))))

(defrecord MatrixSlot [container index x y]
  Object
  (isItemValid [_ stack]
    (matrix/valid-slot? (:matrix container) index stack))
  
  (getStack [_]
    (get-slot-stack container index))
  
  (putStack [_ stack]
    (set-slot-stack container index stack))
  
  (getSlotIndex [_] index))