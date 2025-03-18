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

(defprotocol IScreen
  "Core screen functionality"
  (init [this] "Initialize the screen")
  (render [this state data] "Render the screen")
  (tick [this] "Update screen state")
  (on-close [this] "Handle screen closing"))

(defprotocol IContainer
  "Container functionality"
  (get-slots [this] "Get container slots")
  (can-interact? [this player] "Check if player can interact")
  (transfer-stack [this slot player] "Transfer stack between container and player"))

(defprotocol IScreenFactory
  "Screen factory functionality"
  (create-screen [this container player] "Create screen instance")
  (get-screen-id [this] "Get unique screen identifier"))

(defprotocol IContainerFactory
  "Container factory functionality"
  (create-container [this id player pos] "Create container instance")
  (get-container-id [this] "Get unique container identifier"))

(defprotocol IGuiRegistry
  "GUI registry functionality"
  (register-screen [this factory] "Register screen factory")
  (register-container [this factory] "Register container factory")
  (create-menu [this id player pos] "Create menu for given ID")
  (open-screen [this screen player] "Open screen for player"))