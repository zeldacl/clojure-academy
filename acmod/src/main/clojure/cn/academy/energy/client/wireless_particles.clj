(ns cn.academy.energy.client.wireless-particles
  (:require [cn.academy.energy.api.wireless :as wireless]
            [cn.lambdalib2.util.math :as math]
            [mcmod.particles :as particles]
            [mcmod.render :as render]
            [mcmod.resource :as resource]))

(def ^:private ENERGY_BEAM_TEX
  (resource/create-location "cljacademy" "textures/effects/energy_beam.png"))

(def ^:private random (java.util.Random.))

(defprotocol IEnergyParticle
  (get-alpha [this])
  (get-scale [this])
  (get-color [this])
  (update-motion! [this]))

(defrecord EnergyBeamParticle [world x y z target-x target-y target-z]
  particles/IParticle
  (tick [this]
    (when (> (:age this) (:max-age this))
      (particles/set-expired! this))
    (update-motion! this))
  
  (render [this buffer-in partial-ticks]
    (let [view-pos (render/get-camera-view)
          view-x (:x view-pos)
          view-y (:y view-pos)
          view-z (:z view-pos)]
      (render/add-line 
        buffer-in
        [(- x view-x) (- y view-y) (- z view-z)]
        [(- target-x view-x) (- target-y view-y) (- target-z view-z)]
        (get-color this)
        (get-alpha this))))

  IEnergyParticle
  (get-alpha [this]
    (let [life-ratio (/ (:age this) (:max-age this))]
      (* 0.8 (- 1.0 life-ratio))))
  
  (get-scale [_]
    (+ 0.2 (* 0.1 (.nextFloat random))))
  
  (get-color [_]
    {:r 0.2 :g 0.6 :b 1.0})
  
  (update-motion! [this]
    (let [dx (- target-x x)
          dy (- target-y y)
          dz (- target-z z)
          dist (math/sqrt (+ (* dx dx) (* dy dy) (* dz dz)))
          speed 0.1]
      (particles/set-motion! this (* dx speed) (* dy speed) (* dz speed)))))

(defn spawn-beam! [world x y z target-x target-y target-z]
  (let [particle (map->EnergyBeamParticle 
                   {:world world 
                    :x x :y y :z z 
                    :target-x target-x :target-y target-y :target-z target-z
                    :age 0
                    :max-age 20
                    :motion [0 0 0]})]
    (particles/add-particle! world particle x y z)
    particle))

(defn spawn-connection-particles! [world source target]
  (let [source-pos (wireless/get-position source)
        target-pos (wireless/get-position target)]
    (spawn-beam! world 
                 (:x source-pos) (:y source-pos) (:z source-pos)
                 (:x target-pos) (:y target-pos) (:z target-pos))))