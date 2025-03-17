(ns cn.academy.block.matrix-gui
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IMatrixGui
  (create-container [this matrix player])
  (create-gui [this container])
  (get-inventory-slots [this])
  (draw-background [this screen-width screen-height])
  (draw-foreground [this mouse-x mouse-y])
  (handle-slot-click [this slot button]))

(defrecord MatrixGui [state]
  IMatrixGui
  (create-container [_ matrix player]
    {:matrix matrix
     :player player
     :slots (get-inventory-slots)})
  
  (create-gui [_ container]
    (reset! state container))
  
  (get-inventory-slots [_]
    [{:id :core-slot :x 80 :y 35}
     {:id :plate-slot-1 :x 60 :y 55}
     {:id :plate-slot-2 :x 80 :y 55}
     {:id :plate-slot-3 :x 100 :y 55}])
  
  (draw-background [_ screen-width screen-height]
    {:width 176
     :height 166
     :texture "academy:textures/gui/matrix.png"
     :draw-commands
     [(fn [ctx]
        (let [{:keys [matrix]} @state]
          (draw-energy-bar ctx (matrix/get-core-level matrix))))
      (fn [ctx]
        (draw-slots ctx (get-inventory-slots)))]})
  
  (draw-foreground [_ mouse-x mouse-y]
    (let [{:keys [matrix]} @state]
      {:title "Matrix"
       :energy-text (str (matrix/get-core-level matrix) " / " (matrix/get-plate-count matrix))}))
  
  (handle-slot-click [_ slot button]
    (let [{:keys [matrix]} @state]
      (case (:id slot)
        :core-slot (update-core matrix button)
        (:plate-slot-1 :plate-slot-2 :plate-slot-3) 
        (update-plate matrix slot button)
        nil))))

(defn create-gui []
  (->MatrixGui (atom {})))