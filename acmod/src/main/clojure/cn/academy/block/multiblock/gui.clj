(ns cn.academy.block.multiblock.gui
  (:require [cn.academy.block.multiblock.machine-state :as machine]
            [cn.academy.block.multiblock.recipes :as recipes]
            [cn.academy.api.gui :as gui])
  (:import [net.minecraft.client.gui.GuiScreen]
           [net.minecraft.inventory Container]))

(def ^:private GUI_TEXTURE (ResourceLocation. "academy" "textures/gui/multiblock.png"))

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
    (gui/->Container
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
    (gui/->Screen
      container
      (fn [screen]
        ;; Draw energy bar
        (gui/draw-energy-bar screen
                            10 10
                            (machine/get-energy machine)
                            (:max-energy machine))
        
        ;; Draw progress bar if active
        (when (machine/is-active? machine)
          (gui/draw-progress-bar screen
                                80 35
                                (recipes/get-progress recipe-handler)))
        
        ;; Draw recipe slots
        (gui/draw-item-slots screen
                            50 20
                            (recipes/get-input-slots recipe-handler))
        
        (gui/draw-item-slots screen
                            110 20
                            (recipes/get-output-slots recipe-handler)))))
  
  (draw-background [_ gui mouse-x mouse-y]
    (let [x (.getGuiLeft gui)
          y (.getGuiTop gui)]
      ;; Draw main background
      (.bindTexture (.getMinecraft gui) GUI_TEXTURE)
      (.blit gui x y 0 0 176 166)
      
      ;; Draw energy bar
      (when (satisfies? machine/IMachineState machine)
        (let [energy (machine/get-energy machine)
              max-energy (:max-energy machine)
              height (int (* 50 (/ energy max-energy)))]
          (.blit gui (+ x 8) (+ y (- 63 height))
                 176 (- 50 height) 16 height)))
      
      ;; Draw progress bar
      (when (satisfies? recipes/IRecipeHandler recipe-handler)
        (when-let [progress (recipes/get-progress recipe-handler)]
          (when-let [recipe (:current-recipe @(:state-atom recipe-handler))]
            (let [width (int (* 24 (/ progress (:process-time recipe))))]
              (.blit gui (+ x 79) (+ y 34)
                     176 50 width 17)))))))
  
  (draw-foreground [_ gui mouse-x mouse-y]
    ;; Draw labels and text
    (.drawString (.getFontRenderer gui) 
                "Multiblock Machine"
                8 6 4210752))
  
  (draw-tooltips [_ gui mouse-x mouse-y]
    (let [x (.getGuiLeft gui)
          y (.getGuiTop gui)]
      ;; Draw energy tooltip
      (when (and (>= mouse-x (+ x 8)) (<= mouse-x (+ x 24))
                 (>= mouse-y (+ y 13)) (<= mouse-y (+ y 63)))
        (.renderTooltip gui 
                       [(str (machine/get-energy machine) 
                            " / " 
                            (:max-energy machine) 
                            " FE")]
                       mouse-x mouse-y))))
  
  (handle-click [_ gui button]
    (case (.getId button)
      0 (machine/set-active! machine 
                            (not (machine/is-active? machine)))
      nil)))

(defn create-gui-handler
  "Create a new GUI handler for a multiblock machine"
  [machine recipe-handler]
  (->MultiblockGuiHandler machine recipe-handler))