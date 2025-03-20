(ns cn.academy.gui.base
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core]
            [clojure.tools.logging :as log]))

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

(defrecord Slot [index x y valid-fn changed-fn]
  ISlot
  (get-index [this] index)
  (get-x [this] x)
  (get-y [this] y)
  (is-valid? [this item] 
    (if valid-fn
      (valid-fn item)
      true))
  (on-slot-changed [this]
    (when changed-fn
      (changed-fn this))))

(defn create-slot [index x y & {:keys [valid-fn changed-fn]}]
  (->Slot index x y valid-fn changed-fn))

(defn create-inventory-slot [inventory index x y]
  (create-slot index x y
               :valid-fn #(is-valid? inventory index %)
               :changed-fn #(mark-dirty inventory)))

(defrecord BaseScreen [config container slots]
  IScreen
  (init [this]
    (doseq [slot slots]
      (when (satisfies? ISlot slot)
        (.on-slot-changed slot))))
        
  (render [this state data]
    (let [{:keys [width height texture]} config]
      (doto this
        (render-background width height)
        (render-foreground)
        (render-tooltips (:mouse-x data) (:mouse-y data)))))
        
  (tick [this]
    ;; Default tick implementation
    )
    
  (on-close [this]
    ;; Clean up resources
    ))

(defn create-screen [config container slots]
  (->BaseScreen config container slots))

;; Screen factory functions for different GUIs
(defn create-container-screen [registry container player]
  (let [config (->GuiConfig 
                 "textures/gui/container.png"
                 "Container"
                 176 
                 166)
        inventory-slots (for [row (range 3)
                            col (range 9)]
                        (create-inventory-slot 
                          (:inventory container)
                          (+ (* row 9) col)
                          (+ 8 (* col 18))
                          (+ 84 (* row 18))))]
    (create-screen config container inventory-slots)))

(defn create-machine-screen [registry container player]
  (let [config (->GuiConfig
                 "textures/gui/machine.png" 
                 "Machine"
                 176
                 166)
        machine-slots (for [i (range 4)]
                       (create-inventory-slot
                         (:inventory container)
                         i
                         (+ 8 (* i 18))
                         20))]
    (create-screen config container machine-slots)))

(defprotocol IGuiConfig
  "Protocol for GUI configuration"
  (get-texture [this] "Get GUI texture")
  (get-title [this] "Get GUI title")
  (get-width [this] "Get GUI width")
  (get-height [this] "Get GUI height"))

(defrecord GuiConfig [texture title width height]
  IGuiConfig
  (get-texture [this] texture)
  (get-title [this] title) 
  (get-width [this] width)
  (get-height [this] height))