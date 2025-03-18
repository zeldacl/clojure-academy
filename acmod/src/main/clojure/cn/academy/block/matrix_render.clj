(ns cn.academy.block.matrix-render
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IMatrixRender
  "Core matrix rendering protocol"
  (render-base [this] "Render the base model parts")
  (render-core [this] "Render the matrix core")
  (render-plates [this] "Render the matrix plates")
  (render-shields [this time] "Render the shield effects with animation")
  (get-model-info [this] "Get model and texture information"))

(defprotocol IMatrixRenderState
  "Matrix render state management"
  (update-render-state! [this] "Update render state for animations")
  (sync-client-state! [this plate-count] "Sync render state with client"))

(defprotocol IMatrixRenderFactory
  "Factory for creating renderers"
  (create-renderer [this matrix] "Create a renderer for a matrix"))

(defrecord MatrixRenderHandler [matrix model texture render-state]
  IMatrixRender
  (render-base [_]
    {:model model
     :texture texture
     :parts ["Main" "Base"]
     :transform {:translate [0.5 0 0.5]
                :scale [1.0 1.0 1.0]}})
  
  (render-core [_]
    (when (pos? (matrix/get-core-level matrix))
      {:model model
       :texture texture
       :parts ["Core"]
       :effects {:glow true
                :alpha 1.0}}))
  
  (render-plates [_]
    (let [plate-count (matrix/get-plate-count matrix)]
      {:model model
       :texture texture
       :parts ["Plate"]
       :instances (for [i (range plate-count)]
                   {:rotation (* (/ 360.0 plate-count) i)
                    :offset [0 0.25 0]})}))
  
  (render-shields [_ time]
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
                      :phase-offset 40.0
                      :effects {:alpha 0.6
                              :additive true}})}))
  
  (get-model-info [_]
    {:model model
     :texture texture
     :animations {:core-spin {:speed 1.0
                             :axis [0 1 0]}
                  :shield-pulse {:frequency 0.5
                               :min-alpha 0.4
                               :max-alpha 0.8}}})
  
  IMatrixRenderState
  (update-render-state! [_]
    (swap! render-state update :frame inc))
  
  (sync-client-state! [_ plate-count]
    (swap! render-state assoc :plate-count plate-count)))

(defn create-matrix-renderer 
  "Creates a new matrix renderer with given model and texture"
  [model texture]
  (->MatrixRenderHandler nil model texture (atom {:frame 0
                                                 :plate-count 0})))

(defrecord MatrixRenderFactory [resource-manager]
  IMatrixRenderFactory
  (create-renderer [_ matrix]
    (let [model (get-in resource-manager [:models :matrix])
          texture (get-in resource-manager [:textures :matrix])]
      (->MatrixRenderHandler matrix model texture (atom {:frame 0
                                                        :plate-count 0})))))