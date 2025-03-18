(ns cn.academy.gui.core)

(defprotocol IGui
  "Core GUI functionality"
  (get-slots [this] "Get all slots")
  (get-slot [this idx] "Get slot at index")
  (transfer-stack [this from-idx to-idx] "Transfer items between slots")
  (handle-click [this slot button] "Handle slot click")
  (get-title [this] "Get GUI title")
  (get-texture [this] "Get GUI texture path")
  (get-size [this] "Get GUI dimensions"))

(defprotocol ISlot
  "Slot functionality"
  (get-x [this] "Get x position")
  (get-y [this] "Get y position")
  (get-index [this] "Get slot index")
  (get-item [this] "Get item in slot")
  (set-item [this item] "Set item in slot")
  (is-valid? [this item] "Check if item valid for slot"))

(defprotocol IRenderer 
  "GUI rendering functionality"
  (render-background [this] "Render background")
  (render-foreground [this] "Render foreground")
  (render-tooltips [this x y] "Render tooltips"))