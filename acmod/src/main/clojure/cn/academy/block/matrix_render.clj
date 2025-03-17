(ns cn.academy.block.matrix-render
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IMatrixRender
  (render-base [this])
  (render-shields [this matrix time])
  (get-model-info [this]))

(defprotocol IMatrixRenderState
  (update-render-state! [this])
  (sync-client-state! [this plate-count]))

(defrecord MatrixRenderHandler [matrix model texture render-state]
  IMatrixRender
  (render-base [_]
    {:model model
     :texture texture
     :parts ["Main" "Core"]})

  (render-shields [_ matrix time]
    (let [plate-count (matrix/get-plate-count matrix)
          has-core? (pos? (matrix/get-core-level matrix))
          shield-count (if (and (= plate-count 3) has-core?) 3 0)
          theta (/ 360.0 shield-count)
          phase (mod (* time 50.0) 360.0)]
      {:shield-count shield-count
       :model model
       :texture texture
       :parts ["Shield"]
       :render-info (for [i (range shield-count)]
                     {:rotation (+ phase (* theta i))
                      :height (* 0.1 (Math/sin (+ (* time 1.111) (* 40.0 i))))
                      :phase-offset 40.0})}))
  
  (get-model-info [_]
    {:model model
     :texture texture})
  
  IMatrixRenderState
  (update-render-state! [_]
    (swap! render-state update :frame inc))
  
  (sync-client-state! [_ plate-count]
    (swap! render-state assoc :plate-count plate-count)))

(defn create-matrix-renderer [model texture]
  (->MatrixRenderHandler nil model texture (atom {:frame 0
                                                 :plate-count 0})))