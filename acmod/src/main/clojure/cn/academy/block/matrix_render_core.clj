(ns cn.academy.block.matrix-render-core
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IMatrixRenderer 
  (render-tile [this pos partial-ticks])
  (render-matrix [this])
  (render-core [this])
  (render-plates [this])
  (render-shield [this time]))

(defprotocol IMatrixRenderState
  (tick-animation [this])
  (get-animation-state [this])
  (update-shield-state [this])
  (get-shield-rotation [this]))

(defrecord MatrixRenderState [state-atom]
  IMatrixRenderState
  (tick-animation [_]
    (swap! state-atom update :animation-time + 0.05))
  
  (get-animation-state [_]
    {:time (:animation-time @state-atom)
     :shield-rotation (mod (* (:animation-time @state-atom) 50.0) 360.0)})
  
  (update-shield-state [_]
    (swap! state-atom update :shield-frame inc))
  
  (get-shield-rotation [_]
    (let [{:keys [shield-frame]} @state-atom
          base-rotation (* shield-frame 2.0)
          phase (/ shield-frame 20.0)]
      {:rotation base-rotation
       :height (* 0.1 (Math/sin (* phase Math/PI)))})))

(defrecord MatrixRenderCore [matrix render-state model]
  IMatrixRenderer
  (render-tile [this pos partial-ticks]
    (doto this
      render-matrix
      render-core
      render-plates
      (render-shield (:time (get-animation-state render-state)))))
  
  (render-matrix [_]
    {:model model
     :part "Main"})
  
  (render-core [_]
    (when (pos? (matrix/get-core-level matrix))
      {:model model
       :part "Core"
       :glow true}))
  
  (render-plates [_]
    (let [plate-count (matrix/get-plate-count matrix)]
      (for [i (range plate-count)]
        {:model model
         :part (str "Plate" i)
         :transform {:translate [0 (* i 0.25) 0]}})))
  
  (render-shield [_ time]
    (when (and (= (matrix/get-plate-count matrix) 3)
               (pos? (matrix/get-core-level matrix)))
      (let [shield-state (get-shield-rotation render-state)]
        {:model model
         :part "Shield"
         :transform {:rotate [0 (:rotation shield-state) 0]
                    :translate [0 (:height shield-state) 0]}}))))

(defn create-render-state []
  (->MatrixRenderState (atom {:animation-time 0.0
                             :shield-frame 0})))

(defn create-renderer [matrix model]
  (->MatrixRenderCore matrix 
                      (create-render-state)
                      model))