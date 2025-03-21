(ns cn.academy.tech-system.energy-system.client.renderer
  (:require [cn.academy.tech-system.energy-system.client.particles :as particles]
            [cn.academy.tech-system.energy-system.client.range-visualizer :as range-viz]
            [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [mcmod.client.render :as render]
            [mcmod.world :as world]
            [clojure.tools.logging :as log]))

(defprotocol IEnergyRenderer
  (render-tile-entity [this te x y z partial-ticks])
  (update-effects [this te])
  (render-hud [this player world pos]))

(defrecord EnergyRenderer [settings-atom]
  IEnergyRenderer
  (render-tile-entity [_ te x y z partial-ticks]
    (when (wireless/is-wireless-node? te)
      (let [energy (wireless/get-energy te)
            max-energy (wireless/get-max-energy te)
            ratio (/ energy max-energy)]
        ;; Only render effects if node is active and has energy
        (when (and (wireless/is-active? te) (pos? ratio))
          (particles/spawn-node-particles! 
            (world/get-world te)
            {:x x :y y :z z}
            ratio)))))
  
  (update-effects [_ te]
    (when (wireless/is-wireless-node? te)
      (let [world (world/get-world te)
            pos (wireless/get-position te)]
        ;; Update connected node effects
        (when-let [connections (wireless/get-connections te)]
          (doseq [target connections]
            (particles/spawn-connection-particles! world te target))))))
  
  (render-hud [_ player world pos]
    (when-let [te (world/get-tile-entity world pos)]
      (when (wireless/is-wireless-node? te)
        (let [energy (wireless/get-energy te)
              max-energy (wireless/get-max-energy te)
              active? (wireless/is-active? te)
              connections (count (wireless/get-connections te))
              max-connections (wireless/get-capacity te)]
          ;; Render energy stats overlay
          (render/draw-string 
            (format "Energy: %d / %d FE" energy max-energy)
            10 10 0xFFFFFF)
          (render/draw-string
            (format "Status: %s" (if active? "Active" "Inactive"))
            10 20 (if active? 0x55FF55 0xFF5555))
          (render/draw-string
            (format "Connections: %d / %d" connections max-connections)
            10 30 0xFFFFFF))))))

(defn create-renderer []
  (->EnergyRenderer (atom {:show-particles true
                          :show-hud true})))