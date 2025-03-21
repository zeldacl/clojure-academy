(ns cn.academy.energy.capability.wireless-capability-provider
  (:require [cn.academy.energy.capability.wireless-node-capability :as node-cap]
            [cn.academy.energy.api.wireless :as wireless]
            [mcmod.capabilities :as cap]))

(defprotocol IWirelessCapability
  (get-capability [this cap dir])
  (invalidate-caps [this]))

(defrecord WirelessNodeProvider [node caps-atom]
  IWirelessCapability
  (get-capability [_ cap dir]
    (when (= cap (node-cap/get-capability))
      (or (get @caps-atom :node)
          (let [storage (node-cap/create-storage 100000.0 20.0 4)]
            (swap! caps-atom assoc :node
                   (cap/create-lazy-optional storage))
            (get @caps-atom :node)))))
  
  (invalidate-caps [_]
    (some-> (get @caps-atom :node) (cap/invalidate))
    (reset! caps-atom {}))

  cap/ICapabilityProvider
  (get-capability [this capability-type side]
    (get-capability this capability-type side)))

(defrecord WirelessMatrixProvider [matrix caps-atom]
  IWirelessCapability
  (get-capability [_ cap dir]
    (when (= (cap/get-name cap) "academy:wireless_matrix")
      (or (get @caps-atom :matrix)
          (let [handler (wireless/create-matrix-handler matrix)]
            (swap! caps-atom assoc :matrix
                   (cap/create-lazy-optional handler))
            (get @caps-atom :matrix)))))
  
  (invalidate-caps [_]
    (some-> (get @caps-atom :matrix) (cap/invalidate))
    (reset! caps-atom {}))

  cap/ICapabilityProvider
  (get-capability [this capability-type side]
    (get-capability this capability-type side)))

(defn create-node-provider [node]
  (->WirelessNodeProvider node (atom {})))

(defn create-matrix-provider [matrix]
  (->WirelessMatrixProvider matrix (atom {})))