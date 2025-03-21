(ns cn.academy.blocks.block_matrix.wireless-matrix
  (:require [mcmod.protocols :refer :all]
            [mcmod.capabilities :as cap]
            [mcmod.gui :as gui]
            [mcmod.materials :as materials]
            [cn.academy.blocks.block_matrix.component :as component]
            [cn.academy.blocks.block_matrix.registry :as registry]))

;; ...existing code...

(defrecord WirelessMatrix []
  IBlock
  (get-properties [this]
    {:material (materials/get-material :iron)
     :hardness 4.0
     :resistance 20.0
     :light-level 7})
  
  ;; ...existing code...
  
  (on-activated [this pos data]
    (let [{:keys [world player]} data]
      (when-not (.isClientSide world)
        (gui/open-gui player world pos "wireless_matrix"))
      true))
  
  (on-placed [this pos data]
    nil)
  
  (on-removed [this pos]
    nil)

  cap/ICapabilityProvider
  (has-capability? [this capability-type side]
    (= (.getName capability-type) "forge:energy"))
  
  (get-capability [this capability-type side]
    (when (has-capability? this capability-type side)
      (component/create-energy 100000)))
  
  (invalidate-capabilities [this]
    nil)

  gui/IGuiProvider
  (create-container [this player inv]
    (registry/get-container-factory "wireless_matrix"))
  
  (get-gui-id [this]
    "wireless_matrix"))

(defn create-wireless-matrix []
  (->WirelessMatrix))