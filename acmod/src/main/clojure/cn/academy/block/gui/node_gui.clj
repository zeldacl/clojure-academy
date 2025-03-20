(ns cn.academy.block.gui.node-gui
  (:require [mcmod.protocols :refer :all]))

(def ^:private GUI_WIDTH 176)
(def ^:private GUI_HEIGHT 166)

(defrecord NodeGui [container tile-entity widgets]
  IGUI
  (init [this]
    ;; Initialize GUI components
    (let [x (quot (- (.width this) GUI_WIDTH) 2)
          y (quot (- (.height this) GUI_HEIGHT) 2)]
      
      ;; Add name field
      (let [name-field (create-text-field this
                                         {:x (+ x 8)
                                          :y (+ y 20)
                                          :width 160
                                          :height 12
                                          :text (.getNodeName tile-entity)})]
        (.setMaxLength name-field 32)
        (swap! widgets assoc :name-field name-field))
      
      ;; Add password field  
      (let [pwd-field (create-text-field this
                                        {:x (+ x 8)
                                         :y (+ y 45)
                                         :width 160
                                         :height 12
                                         :text (.getPassword tile-entity)})]
        (.setMaxLength pwd-field 32)
        (swap! widgets assoc :pwd-field pwd-field))
      
      ;; Add enable button
      (let [btn (create-button this
                              {:x (+ x 10)
                               :y (+ y 65)
                               :width 60
                               :height 20
                               :text (if (.isEnabled tile-entity) "Enabled" "Disabled")
                               :callback #(.setEnabled tile-entity (not (.isEnabled tile-entity)))})]
        (swap! widgets assoc :toggle-btn btn))))

  (draw-background [this renderer mouse-x mouse-y]
    (let [x (quot (- (.width this) GUI_WIDTH) 2)
          y (quot (- (.height this) GUI_HEIGHT) 2)]
      
      ;; Draw main background
      (bind-texture renderer "academy:textures/gui/node.png")
      (draw-textured-rect renderer x y 0 0 GUI_WIDTH GUI_HEIGHT)
      
      ;; Draw energy bar
      (let [energy-pct (/ (.getEnergy tile-entity) (.getMaxEnergy tile-entity))
            bar-height (int (* 50 energy-pct))]
        (draw-textured-rect renderer 
                          (+ x 156) (+ y (- 63 bar-height))
                          176 (- 50 bar-height)
                          16 bar-height))))
  
  (draw-foreground [this renderer mouse-x mouse-y]
    ;; Draw text labels
    (let [title (.getNodeName tile-entity)
          energy (format "%.0f / %.0f RF" 
                        (.getEnergy tile-entity)
                        (.getMaxEnergy tile-entity))
          bandwidth (format "Bandwidth: %d RF/t" (.getBandwidth tile-entity))
          range (format "Range: %d blocks" (.getRange tile-entity))]
      
      (draw-string renderer (.font this) title 8 6 4210752)
      (draw-string renderer (.font this) energy 8 85 4210752)
      (draw-string renderer (.font this) bandwidth 8 95 4210752) 
      (draw-string renderer (.font this) range 8 105 4210752))
    
    ;; Draw widgets
    (doseq [widget (vals @widgets)]
      (render widget renderer mouse-x mouse-y 0)))
  
  (handle-mouse-click [this mouse-x mouse-y button]
    (doseq [widget (vals @widgets)]
      (mouse-clicked widget mouse-x mouse-y button)))
  
  (handle-key-press [this key scancode modifiers]
    (let [name-field (:name-field @widgets)
          pwd-field (:pwd-field @widgets)]
      (when (.isFocused name-field)
        (key-pressed name-field key scancode modifiers)
        (.setNodeName tile-entity (.getText name-field)))
      (when (.isFocused pwd-field)
        (key-pressed pwd-field key scancode modifiers)
        (.setPassword tile-entity (.getText pwd-field))))))

(defn create-gui [container]
  (->NodeGui container 
            (.getTileEntity container)
            (atom {})))

;; Export for Java interop
(gen-class
  :name cn.academy.block.gui.GuiNode
  :prefix "gui-"
  :init init
  :constructors {[Object] []}
  :state state
  :methods [[getContainer [] Object]])

(defn gui-init [container]
  [[] (create-gui container)])

(defn gui-getContainer [this]
  (:container (.state this)))