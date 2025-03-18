(ns cn.academy.energy.capability.wireless-capability-provider
  (:require [cn.academy.energy.capability.wireless-node-capability :as node-cap]
            [cn.academy.energy.api.wireless :as wireless])
  (:import [net.minecraftforge.common.capabilities ICapabilityProvider]
           [net.minecraftforge.common.util LazyOptional NonNullSupplier]
           [net.minecraft.util Direction]))

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
                   (LazyOptional/of 
                     (reify NonNullSupplier
                       (get [_] storage))))
            (get @caps-atom :node)))))
  
  (invalidate-caps [_]
    (some-> (get @caps-atom :node) .invalidate)
    (reset! caps-atom {}))

  ICapabilityProvider
  (getCapability [this cap dir]
    (get-capability this cap dir)))

(defrecord WirelessMatrixProvider [matrix caps-atom]
  IWirelessCapability
  (get-capability [_ cap dir]
    (when (= (.getName cap) "academy:wireless_matrix")
      (or (get @caps-atom :matrix)
          (let [handler (wireless/create-matrix-handler matrix)]
            (swap! caps-atom assoc :matrix
                   (LazyOptional/of 
                     (reify NonNullSupplier
                       (get [_] handler))))
            (get @caps-atom :matrix)))))
  
  (invalidate-caps [_]
    (some-> (get @caps-atom :matrix) .invalidate)
    (reset! caps-atom {}))

  ICapabilityProvider
  (getCapability [this cap dir]
    (get-capability this cap dir)))

(defn create-node-provider [node]
  (->WirelessNodeProvider node (atom {})))

(defn create-matrix-provider [matrix]
  (->WirelessMatrixProvider matrix (atom {})))