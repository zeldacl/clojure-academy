(ns cn.academy.blocks.block-node.gui
  (:require [mcmod.protocols :refer :all]
            [cn.academy.blocks.block-node.tile :as tile]
            [cn.academy.blocks.block-node.ui-components :as ui :refer [GUI_WIDTH GUI_HEIGHT GUI_TEXTURE]]))

(defn- create-widget
  "Create a GUI widget with standard properties and add it to the widgets map"
  [widgets widget-type this props]
  (let [widget (case widget-type
                 :text-field (create-text-field this props)
                 :button (create-button this props))]
    (when widget
      (when-let [max-length (:max-length props)]
        (.setMaxLength widget max-length))
      (swap! widgets assoc (:id props) widget)
      widget)))

(defrecord NodeGui [container tile-entity widgets]
  IGui
  (init [this]
    ;; Initialize GUI components
    (let [{:keys [x y]} (ui/create-centered-coords (.width this) (.height this))]
      
      ;; Add name field
      (create-widget widgets :text-field this
                    {:id :name-field
                     :x (+ x 8)
                     :y (+ y 20)
                     :width 160
                     :height 12
                     :max-length 32
                     :text (ui/get-tile-property tile-entity :name)})
      
      ;; Add password field  
      (create-widget widgets :text-field this
                    {:id :pwd-field
                     :x (+ x 8)
                     :y (+ y 45)
                     :width 160
                     :height 12
                     :max-length 32
                     :text (ui/get-tile-property tile-entity :password)})
      
      ;; Add enable button
      (create-widget widgets :button this
                    {:id :toggle-btn
                     :x (+ x 10)
                     :y (+ y 65)
                     :width 60
                     :height 20
                     :text (if (ui/get-tile-property tile-entity :enabled) "Enabled" "Disabled")
                     :callback #(ui/set-tile-property! 
                                tile-entity 
                                :enabled 
                                (not (ui/get-tile-property tile-entity :enabled)))})))

  (render [this mouse-data render-data]
    (let [{:keys [x y]} (ui/create-centered-coords (.width this) (.height this))
          matrix-stack (:matrix-stack render-data)]
      
      ;; Draw main background
      (bind-texture GUI_TEXTURE)
      (draw-textured-rect x y 0 0 GUI_WIDTH GUI_HEIGHT)
      
      ;; Draw energy bar
      (let [energy (ui/get-tile-property tile-entity :energy)
            max-energy (ui/get-tile-property tile-entity :max-energy)
            energy-height (int (* 50 (/ energy max-energy)))]
        (draw-textured-rect (+ x 166) (+ y 8) 176 0 4 50)  ;; Empty bar
        (when (pos? energy-height)
          (draw-textured-rect (+ x 166) (+ y (- 58 energy-height)) 180 0 4 energy-height))) ;; Filled bar
      
      ;; Draw range indicator
      (let [range (ui/get-tile-property tile-entity :range)]
        (draw-string range 8 105 4210752))
      
      ;; Draw widgets
      (doseq [widget (vals @widgets)]
        (render widget mouse-data render-data))))

  (on-close [this]
    ;; Save any pending changes
    (let [name-field (:name-field @widgets)
          pwd-field (:pwd-field @widgets)]
      (ui/set-tile-property! tile-entity :name (.getText name-field))
      (ui/set-tile-property! tile-entity :password (.getText pwd-field))))

  (get-title [_] "Wireless Node")
  
  (get-texture [_] GUI_TEXTURE)

  IGuiContainer  
  (get-slots [_] 
    (.getSlots container))

  (is-valid? [_ player]
    (.stillValid container player))

  (sync-data [_]
    (.broadcastChanges container))

  (on-button-clicked [_ player button-id]
    (when-let [btn (get @widgets button-id)]
      ((:callback btn)))))

(defn create-gui [container]
  (->NodeGui container 
            (.getTileEntity container)
            (atom {})))

;; Export for Java interop
(gen-class
  :name cn.academy.blocks.block-node.GuiNode
  :prefix "gui-"
  :init init
  :constructors {[Object] []}
  :state state
  :methods [[getContainer [] Object]])

(defn gui-init [container]
  [[] (create-gui container)])

(ui/define-java-accessor "gui" "getContainer" :container)