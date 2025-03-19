(ns cn.li.bridge.gui.api)

(defprotocol IGui
  "Core GUI functionality"
  (init [this] "Initialize GUI")
  (render [this mouse-data render-data] "Render GUI")
  (on-close [this] "Handle GUI closing")
  (get-title [this] "Get GUI title")
  (get-texture [this] "Get GUI texture"))

(defprotocol IGuiContainer
  "Container functionality"
  (get-slots [this] "Get container slots")
  (is-valid? [this player] "Check if player can interact")
  (on-closed [this player] "Handle container closing")
  (sync-data [this] "Sync container data")
  (on-button-clicked [this player button-id] "Handle button click"))

(defprotocol IGuiFactory
  "GUI factory functionality"
  (create-gui [this container player] "Create GUI for container")
  (register-gui [this registry gui-id] "Register GUI type"))