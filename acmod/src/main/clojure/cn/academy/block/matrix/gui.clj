(ns cn.academy.block.matrix.gui
  (:require [cn.academy.block.matrix :as matrix]))

(def ^:private layout
  {:core {:x 78 :y 36}
   :plates [[78 11] [53 60] [104 60]]
   :energy-bar {:x 176 :y 13}
   :inventory-start [8 84]
   :hotbar-start [8 142]})

(defprotocol IMatrixGui
  (get-slots [this])
  (get-slot [this idx])
  (transfer-stack [this from-idx to-idx])
  (handle-click [this slot button])
  (get-title [this])
  (get-texture [this])
  (get-size [this]))

(defrecord MatrixSlot [index x y matrix slot-type]
  Object
  (get-x [_] x)
  (get-y [_] y)
  (get-index [_] index)
  (get-item [_] 
    (case slot-type
      :core (matrix/get-core-item matrix)
      :plate (matrix/get-plate-item matrix index)))
  (set-item [_ item]
    (case slot-type
      :core (matrix/set-core-item! matrix item)
      :plate (matrix/set-plate-item! matrix index item)))
  (is-valid? [_ item]
    (case slot-type
      :core (matrix/valid-core? matrix item)
      :plate (matrix/valid-plate? matrix item))))

(defrecord MatrixGui [matrix]
  IMatrixGui
  (get-slots [_]
    (concat
     ; Core slot
     [(->MatrixSlot 3 
                    (get-in layout [:core :x])
                    (get-in layout [:core :y])
                    matrix 
                    :core)]
     ; Plate slots
     (map-indexed 
      (fn [idx [x y]]
        (->MatrixSlot idx x y matrix :plate))
      (:plates layout))))
  
  (get-slot [this idx]
    (nth (get-slots this) idx nil))
  
  (transfer-stack [this from-idx to-idx]
    (when-let [from-slot (get-slot this from-idx)]
      (when-let [to-slot (get-slot this to-idx)]
        (when-let [item (.get-item from-slot)]
          (when (.is-valid? to-slot item)
            (.set-item to-slot item)
            (.set-item from-slot nil))))))
  
  (handle-click [this slot button]
    (when-let [slot (get-slot this slot)]
      {:type (if (< (.get-index slot) 4)
               :to-player
               :to-matrix)
       :slot (.get-index slot)}))
  
  (get-title [_]
    "Wireless Energy Matrix")
  
  (get-texture [_]
    "academy:textures/gui/matrix.png")
  
  (get-size [_]
    {:width 176 :height 166}))

(defn create-gui [matrix]
  (->MatrixGui matrix))