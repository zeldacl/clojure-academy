(ns cn.academy.block.matrix-util)

;; Core matrix structure definition
(def default-structure
  [[-1 0 -1] [0 0 -1] [1 0 -1]
   [-1 0 0]  [0 0 0]  [1 0 0]
   [-1 0 1]  [0 0 1]  [1 0 1]])

(defn get-default-structure []
  default-structure)

;; Position and coordinate utilities
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

;; Structure validation utilities
(defn valid-structure-placement? [pos structure]
  (every? #(can-replace-block? (calc-offset pos %)) structure))

(defn try-form-structure [pos structure]
  (when (valid-structure-placement? pos structure)
    (mapv #(calc-offset pos %) structure)))

;; Position serialization
(defn serialize-pos [pos]
  {:x (:x pos) :y (:y pos) :z (:z pos)})

(defn deserialize-pos [data]
  {:x (:x data) :y (:y data) :z (:z data)})

;; Math utilities
(defn in-range? [pos1 pos2 range]
  (<= (calc-distance pos1 pos2) range))

(defn calc-distance [pos1 pos2]
  (Math/sqrt (+ (Math/pow (- (:x pos2) (:x pos1)) 2)
                (Math/pow (- (:y pos2) (:y pos1)) 2)
                (Math/pow (- (:z pos2) (:z pos1)) 2))))

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