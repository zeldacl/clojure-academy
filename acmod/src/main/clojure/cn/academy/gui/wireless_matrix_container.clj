(ns cn.academy.gui.wireless-matrix-container
  (:require [mcmod.gui :as gui]
            [mcmod.capabilities :as cap])
  (:import [net.minecraft.entity.player PlayerEntity]))

(defrecord WirelessMatrixContainer [tile-entity player inv]
  gui/IContainer
  (transfer-stack-in-slot [this player index]
    ;; No item transfer needed for energy-only GUI
    nil)
  
  (can-interact-with [this player]
    ;; Check if player is within range
    (let [pos (.getPos tile-entity)
          player-pos (.getPosition player)]
      (< (.distanceSq pos player-pos) 64.0)))
  
  (get-slot-count [this]
    0)  ; No inventory slots
  
  ;; Add methods to get energy info for GUI
  (get-energy-stored [this]
    (when-let [energy-storage (cap/get-capability tile-entity "forge:energy" nil)]
      (cap/get-energy-stored energy-storage)))
  
  (get-max-energy-stored [this]
    (when-let [energy-storage (cap/get-capability tile-entity "forge:energy" nil)]
      (cap/get-max-energy-stored energy-storage))))