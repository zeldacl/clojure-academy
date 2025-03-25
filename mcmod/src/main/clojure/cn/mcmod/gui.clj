(ns mcmod.gui
  (:require [mcmod.protocols :refer :all]))

(defprotocol IContainer
  "Protocol for GUI containers"
  (transfer-stack-in-slot [this player index] "Handle slot clicking")
  (can-interact-with [this player] "Check if player can use container")
  (get-slot-count [this] "Get number of slots in container"))

(defprotocol IGuiProvider
  "Protocol for blocks/items that provide GUIs"
  (create-container [this player inv] "Create container instance")
  (get-gui-id [this] "Get unique GUI identifier"))