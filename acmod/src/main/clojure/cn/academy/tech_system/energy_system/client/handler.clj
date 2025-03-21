(ns cn.academy.tech-system.energy-system.client.handler
  (:require [cn.academy.tech-system.energy-system.client.particles :as particles]
            [cn.academy.tech-system.energy-system.client.range-visualizer :as range-viz]
            [cn.academy.tech-system.energy-system.client.renderer :as renderer]
            [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [mcmod.client.event :as event]
            [mcmod.client.keybind :as keybind]
            [mcmod.world :as world]))

;; Create renderer instance
(def ^:private energy-renderer (renderer/create-renderer))

;; Key binding for toggling range visualization
(def ^:private toggle-range-key
  (keybind/create-key "key.toggleEnergyRange" 
                      (keybind/key "R")
                      "category.energySystem"))

;; Event handlers
(defn- on-render-world [event]
  (let [world (:world event)
        partial-ticks (:partial-ticks event)]
    ;; Render node ranges
    (range-viz/render-node-ranges! partial-ticks)))

(defn- on-render-tile-entity [event]
  (let [{:keys [te x y z partial-ticks]} event]
    (renderer/render-tile-entity energy-renderer te x y z partial-ticks)))

(defn- on-render-hud [event]
  (let [{:keys [player world pos]} event]
    (renderer/render-hud energy-renderer player world pos)))

(defn- on-key-input [event]
  (when (keybind/is-key-pressed? toggle-range-key)
    (range-viz/toggle-range-visualization!)))

(defn- on-tile-entity-tick [event]
  (let [te (:tile-entity event)]
    (when (wireless/is-wireless-node? te)
      (renderer/update-effects energy-renderer te))))

;; Registration
(defn register-handlers! []
  (event/register-handler :render-world #'on-render-world)
  (event/register-handler :render-tile-entity #'on-render-tile-entity)
  (event/register-handler :render-hud #'on-render-hud)
  (event/register-handler :key-input #'on-key-input)
  (event/register-handler :tile-entity-tick #'on-tile-entity-tick))