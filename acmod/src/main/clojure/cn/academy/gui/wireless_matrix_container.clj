(ns cn.academy.gui.wireless-matrix-container
  (:require [mcmod.gui :as gui]
            [mcmod.entity :as entity]
            [mcmod.capabilities :as cap]))

(defrecord WirelessMatrixContainer [tile-entity player inv]
  gui/IContainer
  (transfer-stack-in-slot [this player index]
    ;; No item transfer needed for energy-only GUI
    nil)
  
  (can-interact-with [this player]
    ;; Check if player is within range
    (let [pos (:pos tile-entity)
          player-pos (entity/get-position player)]
      (< (entity/distance-sq pos player-pos) 64.0)))
  
  (get-slot-count [this]
    0)  ; No inventory slots
  
  ;; Add methods to get energy info for GUI
  (get-energy-stored [this]
    (when-let [energy-storage (cap/get-capability tile-entity "forge:energy" nil)]
      (cap/get-energy-stored energy-storage)))
  
  (get-max-energy-stored [this]
    (when-let [energy-storage (cap/get-capability tile-entity "forge:energy" nil)]
      (cap/get-max-energy-stored energy-storage))))