(ns cn.academy.gui.base-gui)

(defprotocol IGuiHandler
  "Base protocol for GUI handling"
  (create-screen [this]
    "Create GUI screen")
  (create-container [this]
    "Create container")
  (get-texture-path [this]
    "Get GUI texture path")
  (get-title [this]
    "Get GUI title"))

(defprotocol IGuiContainer
  "Base protocol for containers"
  (get-slots [this]
    "Get container slots")
  (can-interact [this]
    "Check if player can interact")
  (handle-slot-click [this slot button action]
    "Handle slot click")
  (transfer-stack [this slot]
    "Handle stack transfer"))

(defprotocol IGuiRenderer
  "Base protocol for GUI rendering"
  (render-background [this width height]
    "Render background")
  (render-foreground [this mouse-x mouse-y]
    "Render foreground")
  (render-tooltip [this x y]
    "Render tooltip"))

(defrecord GuiSlot [index x y filter handler]
  Object
  (accepts? [_ stack]
    (filter stack))
  (on-change [_ stack]
    (handler stack)))

(defrecord GuiElement [x y width height texture-u texture-v handler]
  Object
  (contains? [_ mouse-x mouse-y]
    (and (>= mouse-x x)
         (< mouse-x (+ x width))
         (>= mouse-y y)
         (< mouse-y (+ y height))))
  (on-click [_ button]
    (handler button)))

(defn create-slot 
  "Create a GUI slot"
  [index x y & {:keys [filter handler] 
                :or {filter (constantly true)
                     handler (constantly nil)}}]
  (->GuiSlot index x y filter handler))

(defn create-button
  "Create a GUI button"
  [x y width height texture-u texture-v handler]
  (->GuiElement x y width height texture-u texture-v handler))

(defn create-icon
  "Create a GUI icon"
  [x y texture-u texture-v]
  (->GuiElement x y 16 16 texture-u texture-v nil)))