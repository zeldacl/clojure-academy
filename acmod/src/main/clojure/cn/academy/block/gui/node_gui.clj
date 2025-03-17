(ns cn.academy.block.gui.node-gui
  (:require [cn.academy.api.gui :as gui-api])
  (:import [net.minecraft.util ResourceLocation]))

(def ^:private gui-texture 
  (ResourceLocation. "academy" "textures/guis/node.png"))

(defprotocol INodeGui
  (render-background [this matrix mouse-x mouse-y])
  (render-energy-bar [this matrix energy max-energy])
  (render-slots [this matrix])
  (render-tooltips [this matrix mouse-x mouse-y]))

(defrecord NodeGui [container width height]
  INodeGui
  (render-background [_ matrix mouse-x mouse-y]
    (gui-api/bind-texture gui-texture)
    (gui-api/blit matrix 0 0 0 0 176 166))
  
  (render-energy-bar [_ matrix energy max-energy]
    (let [height (int (* 50 (/ energy max-energy)))]
      (gui-api/bind-texture gui-texture)
      (gui-api/blit matrix 
                    79 65 ; x, y position
                    176 0 ; texture u,v 
                    16 height ; width, height
                    )))
  
  (render-slots [_ matrix]
    (gui-api/bind-texture gui-texture)
    ;; Render item slots
    (gui-api/blit matrix 55 16 0 166 18 18) ; Input slot
    (gui-api/blit matrix 55 52 0 166 18 18)) ; Output slot
  
  (render-tooltips [this matrix mouse-x mouse-y]
    (when (and (>= mouse-x 79) (<= mouse-x 95)
               (>= mouse-y 15) (<= mouse-y 65))
      (let [energy (.. container tile getEnergy)
            max-energy (.. container tile getMaxEnergy)]
        (gui-api/render-tooltip matrix 
                              [(format "Energy: %.0f / %.0f IF" energy max-energy)]
                              mouse-x mouse-y)))))

(defn create-gui [container screen-width screen-height]
  (->NodeGui container screen-width screen-height))