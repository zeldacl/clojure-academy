(ns mcmod.direction
  (:require [mcmod.position :as pos]))

(def DIRECTIONS
  {:north [0 0 -1]
   :south [0 0 1]
   :east [1 0 0]
   :west [-1 0 0]
   :up [0 1 0]
   :down [0 -1 0]})

(def ROTATIONS
  {:north 0
   :east 90
   :south 180
   :west 270})

(def HORIZONTAL_DIRECTIONS
  [:north :south :east :west])

(def VERTICAL_DIRECTIONS
  [:up :down])

(defn get-direction-vector
  "Get vector for direction"
  [direction]
  (get DIRECTIONS direction))

(defn get-opposite
  "Get opposite direction"
  [direction]
  (case direction
    :north :south
    :south :north
    :east :west
    :west :east
    :up :down
    :down :up))

(defn rotate-y
  "Rotate direction around Y axis"
  [direction angle]
  (let [[x _ z] (get-direction-vector direction)
        rad (Math/toRadians angle)
        cos (Math/cos rad)
        sin (Math/sin rad)
        new-x (- (* x cos) (* z sin))
        new-z (+ (* x sin) (* z cos))]
    (cond
      (and (pos? new-x) (zero? new-z)) :east
      (and (neg? new-x) (zero? new-z)) :west  
      (and (zero? new-x) (pos? new-z)) :south
      (and (zero? new-x) (neg? new-z)) :north
      :else direction)))

(defn rotate-position
  "Rotate position around Y axis"
  [pos angle]
  (let [x (pos/get-x pos)
        y (pos/get-y pos)
        z (pos/get-z pos)
        rad (Math/toRadians angle)
        cos (Math/cos rad)
        sin (Math/sin rad)]
    (pos/create-pos
      (- (* x cos) (* z sin))
      y
      (+ (* x sin) (* z cos)))))