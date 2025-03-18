(ns cn.academy.block.matrix-gui
  (:require [cn.academy.gui.core :as gui]))

(def ^:private layout
  {:core {:x 78 :y 36}
   :plates [{:x 78 :y 11}
            {:x 53 :y 60}
            {:x 104 :y 60}]
   :energy-bar {:x 176 :y 13}})

(defrecord MatrixSlot [index x y matrix slot-type]
  gui/ISlot
  (get-x [_] x)
  (get-y [_] y)
  (get-index [_] index)
  (get-item [_] 
    (case slot-type
      :core (get-in matrix [:core :item])
      :plate (get-in matrix [:plates index :item])))
  (set-item [_ item]
    (case slot-type
      :core (assoc-in matrix [:core :item] item)
      :plate (assoc-in matrix [:plates index :item] item)))
  (is-valid? [_ item]
    (case slot-type
      :core (valid-core? matrix item)
      :plate (valid-plate? matrix item))))

(defrecord MatrixGui [matrix]
  gui/IGui
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
        (fn [idx {:keys [x y]}]
          (->MatrixSlot idx x y matrix :plate))
        (:plates layout))))
  
  (get-slot [this idx]
    (nth (get-slots this) idx nil))
  
  (transfer-stack [this from-idx to-idx]
    (when-let [from-slot (get-slot this from-idx)]
      (when-let [to-slot (get-slot this to-idx)]
        (when-let [item (get-item from-slot)]
          (when (is-valid? to-slot item)
            (set-item to-slot item)
            (set-item from-slot nil))))))
  
  (handle-click [this slot button]
    (when-let [slot (get-slot this slot)]
      {:type (if (< (get-index slot) 4)
               :to-player
               :to-matrix)
       :slot (get-index slot)}))
  
  (get-title [_]
    "Wireless Energy Matrix")
  
  (get-texture [_]
    "academy:textures/gui/matrix.png")
  
  (get-size [_]
    {:width 176 :height 166}))

(defrecord MatrixRenderer [gui]
  gui/IRenderer
  (render-background [_]
    {:texture (gui/get-texture gui)
     :regions [[0 0 176 166 0 0]
               [176 13 16 50 176 0]]})
  
  (render-foreground [_]
    (let [matrix (:matrix gui)
          energy-level (get-energy-level matrix)
          plate-count (get-plate-count matrix)
          {:keys [x y]} (:energy-bar layout)
          height (* 50 energy-level)]
      {:energy-bar {:x x
                   :y (- (+ y 50) height)
                   :width 16
                   :height height
                   :u 176
                   :v (- 50 height)}
       :plates (for [idx (range plate-count)
                    :let [{:keys [x y]} (nth (:plates layout) idx)]]
                {:x (dec x)
                 :y (dec y)
                 :width 18
                 :height 18
                 :u 176
                 :v 0})}))
  
  (render-tooltips [_ mouse-x mouse-y]
    (when-let [slot (get-slot-at gui mouse-x mouse-y)]
      (when-let [item (gui/get-item slot)]
        {:lines [(get-item-name item)
                (str "Energy: " (get-energy-stored matrix)
                     "/" (get-energy-capacity matrix))]}))))

(defn create-matrix-gui [matrix]
  (->MatrixGui matrix))

(defn create-matrix-renderer [gui]
  (->MatrixRenderer gui))