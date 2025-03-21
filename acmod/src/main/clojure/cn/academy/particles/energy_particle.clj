(ns cn.academy.particles.energy-particle
  (:require [mcmod.particles :as particles]
            [mcmod.world :as world]))

(defn create-energy-particle [world x y z vx vy vz]
  (particles/create-particle
    :energy
    world
    {:position [x y z]
     :velocity [vx vy vz]
     :lifetime 20
     :render-type :translucent
     :behavior (fn [particle]
                 (let [{:keys [position velocity age]} particle
                       [x y z] position
                       [vx vy vz] velocity]
                   (if (> age 20)
                     (assoc particle :alive false)
                     (assoc particle
                            :position [(+ x vx) (+ y vy) (+ z vz)]
                            :age (inc age)))))}))

(defn spawn-particles [world pos count]
  (dotimes [_ count]
    (let [rand-velocity (fn [] (- (rand 0.1) 0.05))]
      (create-energy-particle world
                             (+ (:x pos) 0.5)
                             (+ (:y pos) 0.5)
                             (+ (:z pos) 0.5)
                             (rand-velocity)
                             (rand-velocity)
                             (rand-velocity)))))