(ns cn.academy.renderers.wireless-matrix-renderer
  (:require [mcmod.capabilities :as cap]
            [mcmod.render :as render]))

(defn render-energy-field [tile-entity matrix partial-ticks buffer light]
  (when-let [energy-storage (cap/get-capability tile-entity "forge:energy" nil)]
    (let [energy-percent (/ (float (cap/get-energy-stored energy-storage))
                           (float (cap/get-max-energy-stored energy-storage)))
          time (/ (System/currentTimeMillis) 1000.0)
          height (* 1.0 energy-percent)]
      
      ;; Use render abstractions from mcmod
      (render/with-matrix-stack matrix
        ;; Translate to center and apply animation
        (render/translate matrix 0.5 0.5 0.5)
        (render/rotate matrix :y (* time 20))
        
        ;; Draw energy field effect using abstract rendering API
        (render/draw-triangle 
          buffer 
          :lightning
          [[-0.5 0.0 -0.5] [0.5 height 0.5] [0.5 0.0 -0.5]]
          [[0.5 0.8 1.0 (* 0.3 energy-percent)]
           [0.5 0.8 1.0 (* 0.7 energy-percent)]
           [0.5 0.8 1.0 (* 0.3 energy-percent)]]))))