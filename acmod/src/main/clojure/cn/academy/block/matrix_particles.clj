(ns cn.academy.block.matrix-particles
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-util :as util]))

(defprotocol IMatrixParticles
  (spawn-core-particles [this pos])
  (spawn-plate-particles [this pos plate-count])
  (spawn-shield-particles [this pos])
  (get-core-particle-color [this])
  (get-shield-particle-color [this]))

(defrecord MatrixParticleSystem [matrix]
  IMatrixParticles
  (spawn-core-particles [_ pos]
    (when (pos? (matrix/get-core-level matrix))
      (let [core-level (matrix/get-core-level matrix)
            particle-count (* core-level 2)]
        (for [_ (range particle-count)]
          {:type :core-glow
           :pos (util/random-sphere-point pos 0.3)
           :velocity (util/random-sphere-velocity 0.02)
           :color (get-core-particle-color matrix)
           :lifetime (+ 20 (rand-int 20))}))))
  
  (spawn-plate-particles [_ pos plate-count]
    (when (pos? plate-count)
      (for [i (range plate-count)]
        {:type :plate-energy
         :pos (-> pos
                 (update :y + (* i 0.25))
                 (util/offset-random 0.2))
         :velocity [0 0.02 0]
         :color [0.3 0.6 1.0]
         :lifetime 40})))
  
  (spawn-shield-particles [_ pos]
    (when (and (= (matrix/get-plate-count matrix) 3)
               (pos? (matrix/get-core-level matrix)))
      (for [_ (range 4)]
        {:type :shield-barrier
         :pos (util/random-sphere-point pos 1.0)
         :velocity (util/random-sphere-velocity 0.01)
         :color (get-shield-particle-color matrix)
         :lifetime 30})))
  
  (get-core-particle-color [_]
    (let [core-level (matrix/get-core-level matrix)]
      [(* 0.3 core-level) 
       (* 0.5 core-level) 
       1.0]))
  
  (get-shield-particle-color [_]
    [0.2 0.4 0.8]))

(defn create-particle-system [matrix]
  (->MatrixParticleSystem matrix))