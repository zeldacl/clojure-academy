(ns cn.academy.block.matrix-render
  (:require [cn.academy.block.matrix-state :as state]))

(defprotocol IRenderer
  "Protocol for matrix rendering"
  (get-texture-location [this]
    "Get texture location")
  (should-render? [this]
    "Check if matrix should be rendered")
  (get-position [this]
    "Get render position")
  (get-core-level [this]
    "Get core energy level")
  (get-plate-count [this]
    "Get number of plates")
  (get-energy-ratio [this]
    "Get energy fill ratio")
  (get-scale [this]
    "Get render scale")
  (get-rotation [this]
    "Get render rotation"))

(defrecord MatrixRenderer [matrix]
  IRenderer
  (get-texture-location [_]
    "academy:textures/blocks/matrix")
  
  (should-render? [_]
    (state/is-formed? (:state matrix)))
  
  (get-position [_]
    (state/get-position (:state matrix)))
  
  (get-core-level [_]
    (state/get-core-level (:state matrix)))
  
  (get-plate-count [_]
    (state/get-plate-count (:state matrix)))
  
  (get-energy-ratio [_]
    (let [stored (energy/get-energy-stored (:energy matrix))
          capacity (energy/get-energy-capacity (:energy matrix))]
      (if (pos? capacity)
        (/ stored capacity)
        0.0)))
  
  (get-scale [this]
    (let [core-level (get-core-level this)
          plate-count (get-plate-count this)]
      (+ 1.0 (* 0.1 core-level plate-count))))
  
  (get-rotation [_]
    (let [tick-time (/ (System/currentTimeMillis) 50.0)]
      [(* tick-time 0.5)       ; x rotation
       (* tick-time 0.25)      ; y rotation
       (* tick-time 0.125)]))) ; z rotation

(defrecord RenderEffect [type props]
  Object
  (get-type [_] type)
  (get-properties [_] props))

(defn create-renderer [matrix]
  (->MatrixRenderer matrix))

(defn get-core-effect [renderer]
  (when (should-render? renderer)
    (->RenderEffect
      :core
      {:scale (get-scale renderer)
       :rotation (get-rotation renderer)
       :energy-ratio (get-energy-ratio renderer)
       :level (get-core-level renderer)})))

(defn get-plate-effects [renderer]
  (when (should-render? renderer)
    (let [count (get-plate-count renderer)
          scale (get-scale renderer)
          [rx ry rz] (get-rotation renderer)]
      (for [i (range count)]
        (->RenderEffect
          :plate
          {:index i
           :scale scale
           :rotation [(+ rx (* i 0.5))
                     (+ ry (* i 0.25))
                     (+ rz (* i 0.125))]})))))

(defn get-shield-effect [renderer time]
  (when (should-render? renderer)
    (->RenderEffect
      :shield
      {:scale (get-scale renderer)
       :alpha (* 0.5 (+ 0.5 (* 0.5 (Math/sin (* time Math/PI 2.0)))))
       :energy-ratio (get-energy-ratio renderer)})))

(defn get-render-data [renderer time]
  {:texture (get-texture-location renderer)
   :position (get-position renderer)
   :effects (concat
             [(get-core-effect renderer)]
             (get-plate-effects renderer)
             [(get-shield-effect renderer time)])})