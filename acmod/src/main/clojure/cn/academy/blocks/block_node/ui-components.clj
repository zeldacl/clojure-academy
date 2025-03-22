(ns cn.academy.blocks.block-node.ui-components
  (:require [mcmod.protocols :refer :all]
            [cn.academy.blocks.block-node.tile :as tile]
            [clojure.tools.logging :as log]))

;; Common UI constants
(def ^:private GUI_WIDTH 176)
(def ^:private GUI_HEIGHT 166)
(def ^:private GUI_TEXTURE "cljacademy:textures/gui/node.png")

;; Shared functionality for UI components
(defn create-centered-coords [width height]
  (let [x (quot (- width GUI_WIDTH) 2)
        y (quot (- height GUI_HEIGHT) 2)]
    {:x x :y y}))

;; Tile entity property access helpers
(defn get-tile-property
  "Access tile entity properties with proper error handling"
  [tile-entity property-name]
  (try
    (case property-name
      :energy (.getEnergy tile-entity)
      :max-energy (.getMaxEnergy tile-entity)
      :range (.getRange tile-entity)
      :bandwidth (.getBandwidth tile-entity)
      :name (.getNodeName tile-entity)
      :password (.getPassword tile-entity)
      :enabled (.isEnabled tile-entity)
      (throw (IllegalArgumentException. (str "Unknown property: " property-name))))
    (catch Exception e
      (log/warn "Error accessing property" property-name "on tile entity:" (.getMessage e))
      nil)))

(defn set-tile-property!
  "Update tile entity properties with proper error handling"
  [tile-entity property-name value]
  (try
    (case property-name
      :name (.setNodeName tile-entity value)
      :password (.setPassword tile-entity value)
      :enabled (.setEnabled tile-entity value)
      :energy (.setEnergy tile-entity value)
      (throw (IllegalArgumentException. (str "Unknown property: " property-name))))
    true
    (catch Exception e
      (log/error "Error setting property" property-name "on tile entity:" (.getMessage e))
      false)))

;; Common Java interop helpers
(defmacro define-java-accessor 
  "Define an accessor method for Java interop"
  [prefix name accessor]
  `(defn ~(symbol (str prefix "-" name)) [this#]
     (~accessor (.state this#))))

;; Export these constants and functions for use in GUI and container modules