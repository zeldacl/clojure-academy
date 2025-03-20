(ns cn.academy.block.gui.node-gui
  (:require [mcmod.protocols :refer :all]
            [cn.academy.block.container.node-container :as container])
  (:import [net.minecraft.client.gui.widget TextFieldWidget Button]
           [net.minecraft.util.text StringTextComponent]
           [net.minecraft.util ResourceLocation]))

(def ^:private TEXTURE (ResourceLocation. "academy" "textures/gui/node.png"))
(def ^:private GUI_WIDTH 176)
(def ^:private GUI_HEIGHT 166)

(defrecord NodeGui [container tile-entity widgets]
  IGUI
  (init [this]
    ;; Initialize GUI components
    (let [x (quot (- (.width this) GUI_WIDTH) 2)
          y (quot (- (.height this) GUI_HEIGHT) 2)]
      
      ;; Add name field
      (let [name-field (TextFieldWidget. (.font this) 
                                       (+ x 8) (+ y 20) 160 12 
                                       (StringTextComponent. "Node Name"))]
        (.setMaxLength name-field 32)
        (.setText name-field (.getNodeName tile-entity))
        (swap! widgets assoc :name-field name-field))
      
      ;; Add password field  
      (let [pwd-field (TextFieldWidget. (.font this)
                                      (+ x 8) (+ y 45) 160 12
                                      (StringTextComponent. "Password"))]
        (.setMaxLength pwd-field 32) 
        (.setText pwd-field (.getPassword tile-entity))
        (swap! widgets assoc :pwd-field pwd-field))
      
      ;; Add enable button
      (let [btn (Button. (+ x 10) (+ y 65) 60 20 
                        (StringTextComponent. (if (.isEnabled tile-entity) "Enabled" "Disabled"))
                        #(.setEnabled tile-entity (not (.isEnabled tile-entity))))]
        (swap! widgets assoc :toggle-btn btn))))

  (draw-background [this renderer mouse-x mouse-y]
    (let [x (quot (- (.width this) GUI_WIDTH) 2)
          y (quot (- (.height this) GUI_HEIGHT) 2)]
      
      ;; Draw main background
      (.bindTexture renderer TEXTURE)
      (.blit this renderer x y 0 0 GUI_WIDTH GUI_HEIGHT)
      
      ;; Draw energy bar
      (let [energy-pct (/ (.getEnergy tile-entity) (.getMaxEnergy tile-entity))
            bar-height (int (* 50 energy-pct))]
        (.blit this renderer 
              (+ x 156) (+ y (- 63 bar-height))
              176 (- 50 bar-height)
              16 bar-height
              256 256))))
  
  (draw-foreground [this renderer mouse-x mouse-y]
    ;; Draw text labels
    (let [title (.getNodeName tile-entity)
          energy (format "%.0f / %.0f RF" 
                        (.getEnergy tile-entity)
                        (.getMaxEnergy tile-entity))
          bandwidth (format "Bandwidth: %d RF/t" (.getBandwidth tile-entity))
          range (format "Range: %d blocks" (.getRange tile-entity))]
      
      (.drawString renderer (.font this) title 8 6 4210752)
      (.drawString renderer (.font this) energy 8 85 4210752)
      (.drawString renderer (.font this) bandwidth 8 95 4210752) 
      (.drawString renderer (.font this) range 8 105 4210752))
    
    ;; Draw widgets
    (doseq [widget (vals @widgets)]
      (.render widget renderer mouse-x mouse-y 0)))
  
  (handle-mouse-click [this mouse-x mouse-y button]
    (doseq [widget (vals @widgets)]
      (.mouseClicked widget mouse-x mouse-y button)))
  
  (handle-key-press [this key scancode modifiers]
    (let [name-field (:name-field @widgets)
          pwd-field (:pwd-field @widgets)]
      (when (.isFocused name-field)
        (.keyPressed name-field key scancode modifiers)
        (.setNodeName tile-entity (.getText name-field)))
      (when (.isFocused pwd-field)
        (.keyPressed pwd-field key scancode modifiers)
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