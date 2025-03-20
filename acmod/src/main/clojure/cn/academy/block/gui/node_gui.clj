(ns cn.academy.block.gui.node-gui
  (:require [mcmod.protocols :refer :all]))

(def ^:private GUI_WIDTH 176)
(def ^:private GUI_HEIGHT 166)
(def ^:private GUI_TEXTURE "cljacademy:textures/gui/node.png")

(defrecord NodeGui [container tile-entity widgets]
  IGui
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
        (swap! widgets assoc :toggle-btn btn)))

  (render [this mouse-data render-data]
    (let [x (quot (- (.width this) GUI_WIDTH) 2)
          y (quot (- (.height this) GUI_HEIGHT) 2)
          matrix-stack (:matrix-stack render-data)]
      
      ;; Draw main background
      (bind-texture GUI_TEXTURE)
      (draw-textured-rect x y 0 0 GUI_WIDTH GUI_HEIGHT)
      
      ;; Draw energy bar
      (let [energy (.getEnergy tile-entity)
            max-energy (.getMaxEnergy tile-entity)
            energy-height (int (* 50 (/ energy max-energy)))]
        (draw-textured-rect (+ x 166) (+ y 8) 176 0 4 50)  ;; Empty bar
        (when (pos? energy-height)
          (draw-textured-rect (+ x 166) (+ y (- 58 energy-height)) 180 0 4 energy-height))) ;; Filled bar
      
      ;; Draw range indicator
      (let [range (.getRange tile-entity)]
        (draw-string range 8 105 4210752))
      
      ;; Draw widgets
      (doseq [widget (vals @widgets)]
        (render widget mouse-data render-data)))

  (on-close [this]
    ;; Save any pending changes
    (let [name-field (:name-field @widgets)
          pwd-field (:pwd-field @widgets)]
      (.setNodeName tile-entity (.getText name-field))
      (.setPassword tile-entity (.getText pwd-field))))

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