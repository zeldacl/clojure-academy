(ns cljacademy.blocks.matrix-gui
  (:require [cljacademy.api.gui :as gui]
            [cljacademy.blocks.matrix-tile :as matrix])
  (:import [net.minecraft.util ResourceLocation]))

(def ^:private gui-texture 
  (ResourceLocation. "cljacademy" "textures/gui/container/wireless_matrix.png"))

(def ^:private energy-bar-texture
  (ResourceLocation. "cljacademy" "textures/gui/container/energy_bar.png"))

(defprotocol IMatrixGui
  (render-background [this screen-width screen-height])
  (render-energy-bar [this energy max-energy])
  (render-slots [this])
  (handle-slot-click [this slot button]))

(defrecord MatrixGui [tile]
  IMatrixGui
  (render-background [_ screen-width screen-height]
    (gui/bind-texture gui-texture)
    (gui/draw-texture 0 0 0 0 176 166))
  
  (render-energy-bar [_ energy max-energy]
    (let [height (int (* 50 (/ energy max-energy)))]
      (gui/bind-texture energy-bar-texture)
      (gui/draw-texture 79 15 176 0 16 height)))
  
  (render-slots [_]
    (gui/bind-texture gui-texture)
    ;; Core slot
    (gui/draw-slot 79 34)
    ;; Plate slots
    (doseq [i (range 3)]
      (gui/draw-slot (+ 52 (* i 18)) 58)))
  
  (handle-slot-click [_ slot button]
    (case (:type slot)
      :core (matrix/handle-core-slot tile slot button)
      :plate (matrix/handle-plate-slot tile slot button)
      nil)))

(defn create-gui [tile]
  (->MatrixGui tile))