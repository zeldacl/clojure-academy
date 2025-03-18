(ns cn.academy.gui.base)

(defprotocol IContainer 
  "Protocol for container functionality"
  (get-slot-count [this] "Get total number of slots")
  (get-slot [this index] "Get slot at index")
  (transfer-stack [this slot-index] "Transfer stack between inventories")
  (can-interact? [this] "Check if player can interact"))

(defprotocol ISlot
  "Protocol for slot functionality"
  (get-index [this] "Get slot index")
  (get-x [this] "Get x position")
  (get-y [this] "Get y position")
  (is-valid? [this item] "Check if item is valid for slot")
  (on-slot-changed [this] "Handle slot content change"))

(defprotocol IScreen
  "Protocol for screen functionality"
  (init [this] "Initialize screen")
  (render-background [this width height] "Render background")
  (render-foreground [this mouse-x mouse-y] "Render foreground")
  (handle-click [this x y button] "Handle mouse click"))

(defprotocol IGuiHandler
  "Protocol for GUI handling"
  (open-gui [this pos] "Open GUI")
  (close-gui [this] "Close GUI")
  (create-container [this] "Create container")
  (create-screen [this container] "Create screen"))

(defrecord Slot [index x y validator handler]
  ISlot
  (get-index [_] index)
  (get-x [_] x)
  (get-y [_] y)
  (is-valid? [_ item] 
    (if validator
      (validator item)
      true))
  (on-slot-changed [_]
    (when handler
      (handler))))

(defn create-slot
  ([index x y]
   (create-slot index x y nil nil))
  ([index x y validator handler]
   (->Slot index x y validator handler)))

(defprotocol IGuiConfig
  "Protocol for GUI configuration"
  (get-texture [this] "Get GUI texture")
  (get-title [this] "Get GUI title")
  (get-width [this] "Get GUI width")
  (get-height [this] "Get GUI height"))