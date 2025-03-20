(ns cn.academy.block.matrix.wireless-matrix
  (:require [mcmod.protocols :refer :all]
            [mcmod.capabilities :as cap]
            [mcmod.gui :as gui]
            [cn.academy.block.matrix.component :as component]
            [cn.academy.block.matrix.registry :as registry])
  (:import [net.minecraft.block.material Material]))

(defrecord WirelessMatrix []
  IBlock
  (get-properties [this]
    {:material Material/IRON
     :hardness 4.0
     :resistance 20.0
     :light-level 7})
  
  (get-material [this]
    Material/IRON)
  
  (get-hardness [this]
    4.0)
  
  (get-resistance [this]
    20.0)
  
  (get-light-level [this]
    7)
  
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