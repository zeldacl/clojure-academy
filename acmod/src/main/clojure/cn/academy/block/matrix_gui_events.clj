(ns cn.academy.block.matrix-gui-events
  (:require [cn.academy.block.matrix-gui-adapter :as gui]))

(defprotocol IGuiEventHandler
  "Protocol for platform-independent GUI event handling"
  (on-init [this]
    "Handle GUI initialization")
  (on-close [this]
    "Handle GUI closing")
  (on-mouse-clicked [this button x y]
    "Handle mouse click events")
  (on-key-pressed [this key-code scancode modifiers]
    "Handle keyboard events")
  (on-slot-click [this slot button action]
    "Handle slot click events"))

(defprotocol IGuiNetworkAdapter
  "Protocol for platform-independent GUI networking"
  (send-button-press [this button-id]
    "Send button press event to server")
  (send-text-update [this field-id text]
    "Send text field update to server")
  (handle-container-update [this data]
    "Handle container data update from server"))

(defrecord MatrixGuiEventHandler [container network-adapter]
  IGuiEventHandler
  (on-init [_]
    (network-adapter/send-init container))
  
  (on-close [_]
    (network-adapter/send-close container))
  
  (on-mouse-clicked [_ button x y]
    (when-let [slot (gui/get-slot-at-position container x y)]
      (handle-slot-click slot button)))
  
  (on-key-pressed [_ key-code scancode modifiers]
    (when (= key-code :escape)  ; Platform-independent key codes
      (gui/close-gui container)))
  
  (on-slot-click [_ slot button action]
    (case action
      :pickup (pickup-from-slot slot)
      :quick-move (quick-move-slot slot)
      :swap (swap-with-held slot)
      :throw (throw-from-slot slot button)
      nil)))

(defn- pickup-from-slot [slot]
  (when-let [stack (gui/get-stack slot)]
    (gui/set-held-stack stack)
    (gui/set-stack slot nil)))

(defn- quick-move-slot [slot]
  (when-let [stack (gui/get-stack slot)]
    (gui/transfer-stack slot)))

(defn- swap-with-held [slot]
  (let [held (gui/get-held-stack)
        slot-stack (gui/get-stack slot)]
    (gui/set-stack slot held)
    (gui/set-held-stack slot-stack)))

(defn- throw-from-slot [slot button]
  (let [amount (if (= button :left) 1 (gui/get-stack-size slot))]
    (gui/remove-stack slot amount)))

(defn create-event-handler [container]
  (->MatrixGuiEventHandler
    container
    (create-network-adapter)))  ; Platform-specific implementation