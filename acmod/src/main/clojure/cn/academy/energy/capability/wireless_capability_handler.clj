(ns cn.academy.energy.capability.wireless-capability-handler
  (:require [cn.academy.energy.api.wireless :as wireless]
            [cn.academy.energy.api.wireless-registry :as registry]
            [mcmod.event :as event]
            [mcmod.resource :as resource]))

(def ^:private WIRELESS_NODE_LOC 
  (resource/create-location "cljacademy" "wireless_node"))

(def ^:private WIRELESS_MATRIX_LOC
  (resource/create-location "cljacademy" "wireless_matrix"))

(defprotocol ICapabilityHandler
  (handle-attach-capabilities [this event]))

(defrecord WirelessCapabilityHandler []
  ICapabilityHandler
  (handle-attach-capabilities [_ event]
    (when (event/is-tile-entity? event)
      (attach-capabilities! event (event/get-object event)))))

(defn attach-capabilities! [event tile]
  (cond 
    (wireless/is-wireless-node? tile)
    (event/add-capability event
      WIRELESS_NODE_LOC
      (registry/create-node-provider tile))
    
    (wireless/is-wireless-matrix? tile)
    (event/add-capability event
      WIRELESS_MATRIX_LOC
      (registry/create-matrix-provider tile))))

(defn on-attach-capabilities [event]
  (when (event/is-tile-entity? event)
    (attach-capabilities! event (event/get-object event))))

(defn create []
  (let [handler (->WirelessCapabilityHandler)]
    ;; Register with event system using mcmod abstractions
    (event/register-handler {:attach-capabilities on-attach-capabilities})
    handler))