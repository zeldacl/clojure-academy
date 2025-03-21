(ns cn.academy.energy.client.renderer
  (:require [cn.academy.energy.client.range-visualizer :as range-viz]
            [cn.academy.energy.api.wireless :as wireless]
            [mcmod.event :as event]
            [mcmod.client :as client]))

;; Define the wireless renderer that handles visualization of wireless nodes and matrices
(defrecord WirelessRenderer []
  event/IEventHandler
  (handle-event [_ event-type event-data]
    (when (= event-type :render-world)
      (on-render-world event-data))))

(defn- on-render-world [event-data]
  (let [world (client/get-current-world)
        player (client/get-player)]
    ;; Render range indicators for nodes and matrices in view
    (doseq [entity (client/get-loaded-tile-entities world)
            :when (or (wireless/is-wireless-node? entity)
                    (wireless/is-wireless-matrix? entity))]
      (cond
        (wireless/is-wireless-node? entity)
        (range-viz/render-node-range entity)
        
        (wireless/is-wireless-matrix? entity) 
        (range-viz/render-matrix-range entity)))))

;; Factory function to create and register the renderer
(defn create []
  (let [renderer (->WirelessRenderer)]
    (event/register-handler 
      {:render-world (fn [e] (on-render-world e))})
    renderer))