(ns cn.academy.tech-system.energy-system.client.particles
  (:require [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [mcmod.client.particle :as particle]
            [mcmod.world :as world]
            [clojure.tools.logging :as log]))

(def ^:private ENERGY_PARTICLE_TEXTURE "cljacademy:particles/energy")
(def ^:private PARTICLE_COLOR [0.2 0.6 1.0])
(def ^:private ENERGY_PARTICLE_LIFETIME 20)

(defn spawn-transfer-particles!
  "Spawn particles showing energy transfer between two points"
  [world start-pos end-pos amount]
  (let [dx (- (:x end-pos) (:x start-pos))
        dy (- (:y end-pos) (:y start-pos))
        dz (- (:z end-pos) (:z start-pos))
        distance (Math/sqrt (+ (* dx dx) (* dy dy) (* dz dz)))
        particle-count (int (* amount (/ distance 2)))]
    (dotimes [i particle-count]
      (let [progress (/ i particle-count)
            x (+ (:x start-pos) (* dx progress))
            y (+ (:y start-pos) (* dy progress))
            z (+ (:z start-pos) (* dz progress))
            velocity-scale 0.02
            vx (* dx velocity-scale)
            vy (* dy velocity-scale)
            vz (* dz velocity-scale)]
        (particle/spawn-colored world
                              ENERGY_PARTICLE_TEXTURE
                              x y z
                              vx vy vz
                              PARTICLE_COLOR
                              ENERGY_PARTICLE_LIFETIME)))))

(defn spawn-node-particles!
  "Spawn particles around an energy node"
  [world pos energy-ratio]
  (let [particle-count (int (* 10 energy-ratio))
        [r g b] PARTICLE_COLOR]
    (dotimes [_ particle-count]
      (let [angle (* 2 Math/PI (rand))
            radius (+ 0.3 (* 0.2 (rand)))
            height (* 0.5 (rand))
            x (+ (:x pos) (* radius (Math/cos angle)))
            y (+ (:y pos) height)
            z (+ (:z pos) (* radius (Math/sin angle)))
            vx (* 0.02 (- (rand) 0.5))
            vy (* 0.02 (rand))
            vz (* 0.02 (- (rand) 0.5))]
        (particle/spawn-colored world
                              ENERGY_PARTICLE_TEXTURE
                              x y z
                              vx vy vz
                              [r g b]
                              ENERGY_PARTICLE_LIFETIME)))))

(defn spawn-connection-particles!
  "Spawn particles showing connection between nodes"
  [world source target]
  (let [source-pos (wireless/get-position source)
        target-pos (wireless/get-position target)
        source-energy (wireless/get-energy source)
        max-energy (wireless/get-max-energy source)
        energy-ratio (/ source-energy max-energy)]
    (when (pos? energy-ratio)
      (spawn-transfer-particles! world source-pos target-pos (* 5 energy-ratio)))))