(ns cn.academy.blocks.block_matrix.render
  (:require [cn.academy.blocks.block_matrix :as matrix]
            [cn.academy.blocks.block_matrix.utils :as utils]
            [mcmod.render :as render]
            [mcmod.resources :as resources]))

(def ^:private matrix-texture
  (resources/create-resource-location "academy" "textures/blocks/wireless_matrix.png"))

(defprotocol IMatrixRenderer
  "Protocol for matrix rendering"
  (get-render-data [this])
  (should-render? [this])
  (render-base [this])
  (render-core [this])
  (render-plates [this])
  (render-shield [this time])
  (update-animation! [this delta-time]))

(defprotocol IMatrixParticles
  "Protocol for matrix particle effects"
  (spawn-core-particles [this pos])
  (spawn-plate-particles [this pos plate-count])
  (spawn-shield-particles [this pos]))

(defrecord MatrixRenderer [matrix model state]
  IMatrixRenderer
  (get-render-data [_]
    {:formed? (matrix/is-formed? matrix)
     :core-level (matrix/get-core-level matrix)
     :plate-count (matrix/get-plate-count matrix)
     :energy (matrix/get-energy-stored matrix)
     :time (:time @state)})
  
  (should-render? [_]
    (matrix/is-formed? matrix))
  
  (render-base [_]
    (render/with-push-matrix
      (render/bind-texture matrix-texture)
      (render/render-model-part model "Main")))
  
  (render-core [_]
    (when (pos? (matrix/get-energy-stored matrix))
      (render/with-push-matrix
        (render/bind-texture matrix-texture)
        (render/render-model-part model "Core"))))
  
  (render-plates [_]
    (let [plate-count (matrix/get-plate-count matrix)]
      (render/with-push-matrix
        (render/bind-texture matrix-texture)
        (doseq [i (range plate-count)]
          (render/with-push-matrix
            (render/translate 0 (* i 0.25) 0)
            (render/render-model-part model (str "Plate" i)))))))
  
  (render-shield [_ time]
    (when (and (= (matrix/get-plate-count matrix) 3)
               (pos? (matrix/get-energy-stored matrix)))
      (let [shield-rotation (* time 2.0)
            shield-height (* 0.1 (Math/sin (* time Math/PI)))]
        (render/with-push-matrix
          (render/bind-texture matrix-texture)
          (render/translate 0 shield-height 0)
          (render/rotate shield-rotation [0 1 0])
          (render/render-model-part model "Shield")))))
  
  (update-animation! [_ delta-time]
    (swap! state update :time + delta-time)
    (when (> (:time @state) 360.0)
      (swap! state assoc :time 0.0))))

(defrecord MatrixParticleSystem [matrix]
  IMatrixParticles
  (spawn-core-particles [_ pos]
    (when (pos? (matrix/get-core-level matrix))
      (let [core-level (matrix/get-core-level matrix)
            color [(* 0.3 core-level) (* 0.5 core-level) 1.0]]
        {:type :core-glow
         :pos pos
         :color color
         :count (* core-level 2)})))
  
  (spawn-plate-particles [_ pos plate-count]
    (when (pos? plate-count)
      {:type :plate-energy
       :pos pos
       :color [0.3 0.6 1.0]
       :count plate-count}))
  
  (spawn-shield-particles [_ pos]
    (when (and (= (matrix/get-plate-count matrix) 3)
               (pos? (matrix/get-core-level matrix)))
      {:type :shield-barrier
       :pos pos
       :color [0.2 0.4 0.8]
       :count 4})))

(defn create-renderer [matrix]
  (let [model (render/load-model "academy:models/block/wireless_matrix")
        state (atom {:time 0.0})]
    (->MatrixRenderer matrix model state)))

(defn create-particle-system [matrix]
  (->MatrixParticleSystem matrix))