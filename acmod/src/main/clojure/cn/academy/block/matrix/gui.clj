(ns cn.academy.block.matrix.gui
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix.container :as container])
  (:import [net.minecraft.util ResourceLocation]))

;; GUI textures
(def ^:private gui-texture 
  (ResourceLocation. "academy" "textures/gui/container/wireless_matrix.png"))

(def ^:private energy-bar-texture
  (ResourceLocation. "academy" "textures/gui/container/energy_bar.png"))

(def ^:private gui-layout
  {:size {:width 176 :height 166}
   :core-slot {:x 79 :y 34}
   :plate-slots (map (fn [i] {:x (+ 52 (* i 18)) :y 58}) (range 3))
   :energy-bar {:x 79 :y 15 :width 16 :height 50}})

(def ^:private layout
  {:core {:x 78 :y 36}
   :plates [[78 11] [53 60] [104 60]]
   :energy-bar {:x 176 :y 13}
   :inventory-start [8 84]
   :hotbar-start [8 142]})

(defprotocol IMatrixGui
  (get-slots [this])
  (get-slot [this idx])
  (transfer-stack [this from-idx to-idx])
  (handle-click [this slot button])
  (get-title [this])
  (get-texture [this])
  (get-size [this])
  (render-background [this screen-width screen-height])
  (render-energy-bar [this energy max-energy])
  (render-slots [this])
  (render-tooltips [this mouse-x mouse-y])
  (handle-slot-click [this slot-id button]))

(defrecord MatrixSlot [index x y matrix slot-type]
  Object
  (get-x [_] x)
  (get-y [_] y)
  (get-index [_] index)
  (get-item [_] 
    (case slot-type
      :core (matrix/get-core-item matrix)
      :plate (matrix/get-plate-item matrix index)))
  (set-item [_ item]
    (case slot-type
      :core (matrix/set-core-item! matrix item)
      :plate (matrix/set-plate-item! matrix index item)))
  (is-valid? [_ item]
    (case slot-type
      :core (matrix/valid-core? matrix item)
      :plate (matrix/valid-plate? matrix item))))

(defrecord MatrixGui [matrix container state]
  IMatrixGui
  (get-slots [_]
    (concat
     ; Core slot
     [(->MatrixSlot 3 
                    (get-in layout [:core :x])
                    (get-in layout [:core :y])
                    matrix 
                    :core)]
     ; Plate slots
     (map-indexed 
      (fn [idx [x y]]
        (->MatrixSlot idx x y matrix :plate))
      (:plates layout))))
  
  (get-slot [this idx]
    (nth (get-slots this) idx nil))
  
  (transfer-stack [this from-idx to-idx]
    (when-let [from-slot (get-slot this from-idx)]
      (when-let [to-slot (get-slot this to-idx)]
        (when-let [item (.get-item from-slot)]
          (when (.is-valid? to-slot item)
            (.set-item to-slot item)
            (.set-item from-slot nil))))))
  
  (handle-click [this slot button]
    (when-let [slot (get-slot this slot)]
      {:type (if (< (.get-index slot) 4)
               :to-player
               :to-matrix)
       :slot (.get-index slot)}))
  
  (get-title [_]
    "Wireless Energy Matrix")
  
  (get-texture [_]
    "academy:textures/gui/matrix.png")
  
  (get-size [_]
    {:width 176 :height 166})

  (render-background [_ screen-width screen-height]
    (let [{:keys [width height]} (:size gui-layout)]
      (gui/bind-texture gui-texture)
      (gui/draw-texture 0 0 0 0 width height)))
  
  (render-energy-bar [_ energy max-energy]
    (let [{:keys [x y width height]} (:energy-bar gui-layout)
          fill-height (int (* height (/ energy max-energy)))]
      (gui/bind-texture energy-bar-texture)
      (gui/draw-texture x y 176 0 width fill-height)))
  
  (render-slots [_]
    (gui/bind-texture gui-texture)
    ;; Core slot
    (let [{:keys [x y]} (:core-slot gui-layout)]
      (gui/draw-slot x y))
    ;; Plate slots
    (doseq [{:keys [x y]} (:plate-slots gui-layout)]
      (gui/draw-slot x y)))
  
  (render-tooltips [_ mouse-x mouse-y]
    (when-let [hover-slot (gui/get-slot-at-position mouse-x mouse-y)]
      (let [energy-info (container/get-energy-info container)]
        (gui/render-tooltip 
         [(str "Energy: " (:current energy-info) " / " (:max energy-info) " FE")]
         mouse-x mouse-y))))
  
  (handle-slot-click [_ slot-id button]
    (container/handle-slot-click container slot-id button)))

(defn create-gui
  "Create new matrix GUI instance"
  [matrix player]
  (let [container (container/create-container matrix player)]
    (->MatrixGui matrix
                container 
                (atom {:hover-slot nil}))))