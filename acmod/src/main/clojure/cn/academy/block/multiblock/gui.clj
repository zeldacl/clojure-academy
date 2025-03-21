(ns cn.academy.block.multiblock.gui
  (:require [cn.academy.block.multiblock.machine-state :as machine]
            [cn.academy.block.multiblock.recipes :as recipes]
            [cn.academy.api.gui :as gui]
            [mcmod.gui :as mcgui]
            [mcmod.resource :as resource]))

(def ^:private GUI_TEXTURE (resource/create-location "academy" "textures/gui/multiblock.png"))

(defprotocol IGuiHandler
  "Protocol for GUI rendering and interaction"
  (create-container [this player inventory])
  (create-gui [this container])
  (draw-background [this gui mouse-x mouse-y] "Draw GUI background")
  (draw-foreground [this gui mouse-x mouse-y] "Draw GUI foreground")
  (draw-tooltips [this gui mouse-x mouse-y] "Draw GUI tooltips")
  (handle-click [this gui button] "Handle button clicks"))

(defrecord MultiblockGuiHandler [machine recipe-handler]
  IGuiHandler
  (create-container [_ player inventory]
    (mcgui/create-container
      (fn []
        {:machine machine
         :recipe-handler recipe-handler
         :player-inv inventory})
      (fn [container slot stack]
        (recipes/can-insert? recipe-handler slot stack))
      (fn [container]
        (when (machine/can-work? machine)
          (recipes/process-recipe! recipe-handler)))))
  
  (create-gui [_ container]
    (mcgui/create-screen
      container
      (fn [screen]
        ;; Draw energy bar
        (mcgui/draw-energy-bar screen
                            10 10
                            (machine/get-energy machine)
                            (:max-energy machine))
        
        ;; Draw progress bar if active
        (when (machine/is-active? machine)
          (mcgui/draw-progress-bar screen
                                80 35
                                (recipes/get-progress recipe-handler)))
        
        ;; Draw recipe slots
        (mcgui/draw-item-slots screen
                            50 20
                            (recipes/get-input-slots recipe-handler))
        
        (mcgui/draw-item-slots screen
                            110 20
                            (recipes/get-output-slots recipe-handler)))))
  
  (draw-background [_ gui mouse-x mouse-y]
    (let [pos (mcgui/get-gui-position gui)]
      ;; Draw main background
      (mcgui/bind-texture gui GUI_TEXTURE)
      (mcgui/draw-texture gui (:x pos) (:y pos) 0 0 176 166)
      
      ;; Draw energy bar
      (when (satisfies? machine/IMachineState machine)
        (let [energy (machine/get-energy machine)
              max-energy (:max-energy machine)
              height (int (* 50 (/ energy max-energy)))]
          (mcgui/draw-texture gui 
                           (+ (:x pos) 8) 
                           (+ (:y pos) (- 63 height))
                           176 (- 50 height) 
                           16 height)))
      
      ;; Draw progress bar
      (when (satisfies? recipes/IRecipeHandler recipe-handler)
        (when-let [progress (recipes/get-progress recipe-handler)]
          (when-let [recipe (:current-recipe @(:state-atom recipe-handler))]
            (let [width (int (* 24 (/ progress (:process-time recipe))))]
              (mcgui/draw-texture gui
                              (+ (:x pos) 79)
                              (+ (:y pos) 34)
                              176 50 
                              width 17)))))))
  
  (draw-foreground [_ gui mouse-x mouse-y]
    ;; Draw labels and text
    (mcgui/draw-string gui 
                     "Multiblock Machine"
                     8 6 
                     4210752))
  
  (draw-tooltips [_ gui mouse-x mouse-y]
    (let [pos (mcgui/get-gui-position gui)]
      ;; Draw energy tooltip
      (when (and (>= mouse-x (+ (:x pos) 8)) 
                 (<= mouse-x (+ (:x pos) 24))
                 (>= mouse-y (+ (:y pos) 13)) 
                 (<= mouse-y (+ (:y pos) 63)))
        (mcgui/render-tooltip gui 
                           [(str (machine/get-energy machine) 
                                " / " 
                                (:max-energy machine) 
                                " FE")]
                           mouse-x mouse-y))))
  
  (handle-click [_ gui button]
    (case (mcgui/get-button-id button)
      0 (machine/set-active! machine 
                            (not (machine/is-active? machine)))
      nil)))

(defn create-gui-handler
  "Create a new GUI handler for a multiblock machine"
  [machine recipe-handler]
  (->MultiblockGuiHandler machine recipe-handler))