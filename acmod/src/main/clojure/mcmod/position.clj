(ns mcmod.position)

(defprotocol IPosition
  "Protocol for position vectors"
  (get-x [this] "Get x coordinate")
  (get-y [this] "Get y coordinate")
  (get-z [this] "Get z coordinate"))

(defrecord Position [x y z]
  IPosition
  (get-x [_] x)
  (get-y [_] y)
  (get-z [_] z))

(defn create-pos
  "Create a new position vector"
  ([x y z]
   (->Position x y z))
  ([coords]
   (->Position (nth coords 0)
               (nth coords 1)
               (nth coords 2))))

(defn add
  "Add two positions or position and offset"
  [pos offset]
  (->Position (+ (get-x pos) (get-x offset))
              (+ (get-y pos) (get-y offset))
              (+ (get-z pos) (get-z offset))))

(defn subtract
  "Subtract positions"
  [pos1 pos2]
  (->Position (- (get-x pos1) (get-x pos2))
              (- (get-y pos1) (get-y pos2))
              (- (get-z pos1) (get-z pos2))))

(defn scale
  "Scale position by factor"
  [pos factor]
  (->Position (* (get-x pos) factor)
              (* (get-y pos) factor) 
              (* (get-z pos) factor)))

(defn distance
  "Get distance between positions"
  [pos1 pos2]
  (let [dx (- (get-x pos1) (get-x pos2))
        dy (- (get-y pos1) (get-y pos2))
        dz (- (get-z pos1) (get-z pos2))]
    (Math/sqrt (+ (* dx dx) 
                  (* dy dy)
                  (* dz dz)))))

(defn manhattan-distance
  "Get Manhattan distance between positions"
  [pos1 pos2]
  (+ (Math/abs (- (get-x pos1) (get-x pos2)))
     (Math/abs (- (get-y pos1) (get-y pos2)))
     (Math/abs (- (get-z pos1) (get-z pos2)))))