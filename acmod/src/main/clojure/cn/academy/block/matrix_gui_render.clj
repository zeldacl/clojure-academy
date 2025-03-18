(ns cn.academy.block.matrix-gui-render
  (:require [cn.academy.block.matrix-gui-adapter :as gui]))

(defprotocol IGuiRenderer
  "Protocol for platform-independent GUI rendering"
  (init-renderer [this]
    "Initialize renderer resources")
  (render-background [this width height]
    "Render GUI background")
  (render-foreground [this mouse-x mouse-y]
    "Render GUI foreground elements")
  (render-tooltip [this mouse-x mouse-y]
    "Render tooltips")
  (cleanup [this]
    "Clean up renderer resources"))

(defprotocol IGuiTextureAdapter
  "Protocol for platform-independent texture handling"
  (bind-texture [this texture-id]
    "Bind texture for rendering")
  (draw-texture [this x y width height u v tex-width tex-height]
    "Draw textured rectangle")
  (draw-text [this text x y color]
    "Draw text string"))

(defprotocol IGuiElementAdapter
  "Protocol for platform-independent GUI elements"
  (create-button [this x y width height text handler]
    "Create button element")
  (create-text-field [this x y width height]
    "Create text input field")
  (create-slot [this inventory index x y]
    "Create inventory slot"))

(defrecord MatrixGuiRenderer [container texture-adapter element-adapter]
  IGuiRenderer
  (init-renderer [_]
    (let [config (gui/->MatrixGuiConfig)]
      {:background-texture "textures/gui/matrix.png"
       :slot-positions (gui/slot-positions config)}))
  
  (render-background [this width height]
    (let [{:keys [background-texture]} (init-renderer this)]
      (bind-texture texture-adapter background-texture)
      (draw-texture texture-adapter 0 0 width height 0 0 256 256)))
  
  (render-foreground [this mouse-x mouse-y]
    (let [{:keys [slot-positions]} (init-renderer this)
          energy-level (gui/get-energy-level container)
          plate-count (gui/get-plate-count container)]
      ; Draw energy bar
      (draw-energy-bar texture-adapter energy-level)
      ; Draw plate indicators
      (draw-plate-indicators texture-adapter plate-count slot-positions)))
  
  (render-tooltip [this mouse-x mouse-y]
    (when-let [slot (gui/get-slot-at-position container mouse-x mouse-y)]
      (when-let [stack (gui/get-stack container slot)]
        (draw-tooltip texture-adapter stack mouse-x mouse-y))))
  
  (cleanup [_]
    nil))

(defn- draw-energy-bar [texture-adapter level]
  (let [height (* 50 level)]
    (draw-texture texture-adapter 
                 176 (- 63 height)  ; x, y
                 16 height          ; width, height
                 176 (- 63 height)  ; u, v
                 16 height)))       ; tex width, height

(defn- draw-plate-indicators [texture-adapter count positions]
  (doseq [[idx pos] (map-indexed vector (:plates positions))
          :when (< idx count)]
    (let [[x y] pos]
      (draw-texture texture-adapter
                   (- x 1) (- y 1)  ; x, y 
                   18 18            ; width, height
                   176 0            ; u, v
                   18 18))))        ; tex width, height

(defn create-gui-renderer [container]
  (->MatrixGuiRenderer 
    container
    (create-texture-adapter)  ; Platform-specific implementation
    (create-element-adapter))) ; Platform-specific implementation