(ns cn.academy.energy.client.range-visualizer
  (:require [cn.academy.energy.api.wireless :as wireless]
            [mcmod.render :as render]
            [mcmod.player :as player]))

(def ^:private SPHERE_PRECISION 32)
(def ^:private RANGE_COLOR {:r 0.2 :g 0.6 :b 1.0 :a 0.15}) ; Light blue, transparent
(def ^:private INACTIVE_COLOR {:r 1.0 :g 0.2 :b 0.2 :a 0.15}) ; Red for inactive

(defn- generate-sphere-vertices [radius precision]
  (for [phi (range 0 Math/PI (/ Math/PI precision))
        theta (range 0 (* 2 Math/PI) (/ (* 2 Math/PI) precision))]
    [(* radius (Math/sin phi) (Math/cos theta))
     (* radius (Math/cos phi))
     (* radius (Math/sin phi) (Math/sin theta))]))

(defn render-range-sphere [pos range active?]
  (render/with-transform
    (render/translate 
      (+ (:x pos) 0.5)
      (+ (:y pos) 0.5)
      (+ (:z pos) 0.5))
    
    ; Set up transparent rendering
    (render/with-state 
      {:blend true
       :depth-test false}
      
      (let [vertices (generate-sphere-vertices range SPHERE_PRECISION)
            color (if active? RANGE_COLOR INACTIVE_COLOR)]
        
        ; Render the sphere as a collection of quads
        (render/begin-batch :quads)
        
        (doseq [[x y z] vertices]
          (render/add-vertex x y z color))
        
        (render/end-batch)))))

(defn render-node-range [node]
  (when (and node (player/is-holding-freq-tool?))
    (let [pos (wireless/get-position node)
          range (wireless/get-range node)
          active? (wireless/is-node-linked node)]
      (render-range-sphere pos range active?))))

(defn render-matrix-range [matrix]
  (when (and matrix (player/is-holding-freq-tool?))
    (let [pos (wireless/get-position matrix)
          range (wireless/get-range matrix)
          active? (wireless/is-matrix-active matrix)]
      (render-range-sphere pos range active?))))