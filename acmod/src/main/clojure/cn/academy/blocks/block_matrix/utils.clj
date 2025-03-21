(ns cn.academy.block.matrix.utils)

(defn calc-distance [pos1 pos2]
  "Calculate distance between two positions"
  (Math/sqrt (+ (Math/pow (- (:x pos2) (:x pos1)) 2)
                (Math/pow (- (:y pos2) (:y pos1)) 2)
                (Math/pow (- (:z pos2) (:z pos1)) 2))))

(defn random-sphere-point [center radius]
  "Generate random point on sphere surface"
  (let [theta (* 2 Math/PI (rand))
        phi (* Math/PI (rand))
        x (* radius (Math/sin phi) (Math/cos theta))
        y (* radius (Math/sin phi) (Math/sin theta))
        z (* radius (Math/cos phi))]
    {:x (+ (:x center) x)
     :y (+ (:y center) y)
     :z (+ (:z center) z)}))

(defn random-sphere-velocity [speed]
  "Generate random velocity vector"
  (let [point (random-sphere-point {:x 0 :y 0 :z 0} speed)]
    [(:x point) (:y point) (:z point)]))

(defn offset-random [pos range]
  "Offset position by random amount within range"
  {:x (+ (:x pos) (- (rand (* 2 range)) range))
   :y (+ (:y pos) (- (rand (* 2 range)) range))
   :z (+ (:z pos) (- (rand (* 2 range)) range))})

(defn in-range? [pos1 pos2 range]
  "Check if positions are within range"
  (<= (calc-distance pos1 pos2) range))

(defn relative-to-absolute [base-pos [rx ry rz]]
  "Convert relative coordinates to absolute position"
  {:x (+ (:x base-pos) rx)
   :y (+ (:y base-pos) ry)
   :z (+ (:z base-pos) rz)})