(ns cn.academy.block.matrix-util
  (:require [cn.academy.block.matrix :as matrix]))

(defn calc-offset [pos facing]
  (let [[x y z] pos]
    (case facing
      :north [x y (dec z)]
      :south [x y (inc z)]
      :west [(dec x) y z]
      :east [(inc x) y z]
      :up [x (inc y) z]
      :down [x (dec y) z])))

(defn calc-sub-positions [pos facing pattern]
  (let [rotation-matrix (get-rotation-matrix facing)
        base-pos (vec pos)]
    (map #(map + base-pos (matrix-multiply rotation-matrix %)) pattern)))

(defn get-rotation-matrix [facing]
  (case facing
    :north [[1 0 0]
            [0 1 0]
            [0 0 1]]
    :south [[-1 0 0]
            [0 1 0]
            [0 0 -1]]
    :west [[0 0 1]
           [0 1 0]
           [-1 0 0]]
    :east [[0 0 -1]
           [0 1 0]
           [1 0 0]]))

(defn matrix-multiply [matrix vec]
  (mapv #(reduce + (map * % vec)) matrix))

(defn in-range? [pos1 pos2 range]
  (<= (Math/sqrt (+ (Math/pow (- (:x pos2) (:x pos1)) 2)
                    (Math/pow (- (:y pos2) (:y pos1)) 2)
                    (Math/pow (- (:z pos2) (:z pos1)) 2)))
      range))

(defn serialize-pos [pos]
  {:x (:x pos)
   :y (:y pos)
   :z (:z pos)})

(defn deserialize-pos [data]
  {:x (:x data)
   :y (:y data)
   :z (:z data)})