(ns cn.academy.block.matrix.render
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IMatrixRenderer
  "Protocol for matrix rendering"
  (get-render-data [this])
  (should-render? [this])
  (get-position [this])
  (get-core-level [this])
  (get-plate-count [this])
  (get-energy-ratio [this])
  (get-scale [this])
  (get-rotation [this]))

(defprotocol IMatrixParticles
  "Protocol for matrix particle effects"
  (spawn-core-particles [this pos])
  (spawn-plate-particles [this pos plate-count])
  (spawn-shield-particles [this pos]))

(defrecord MatrixRenderer [matrix state]
  IMatrixRenderer
  (get-render-data [_]
    {:formed? (matrix/is-formed? matrix)
     :core-level (matrix/get-core-level matrix)
     :plate-count (matrix/get-plate-count matrix)
     :time (:time @state)})
  
  (should-render? [_]
    (matrix/is-formed? matrix))
  
  (get-position [_]
    (matrix/get-position matrix))
  
  (get-core-level [_]
    (matrix/get-core-level matrix))
  
  (get-plate-count [_]
    (matrix/get-plate-count matrix))
  
  (get-energy-ratio [_]
    (let [stored (matrix/get-energy-stored matrix)
          capacity (matrix/get-energy-capacity matrix)]
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
  (->MatrixRenderer matrix (atom {:time 0.0})))

(defn create-particle-system [matrix]
  (->MatrixParticleSystem matrix))