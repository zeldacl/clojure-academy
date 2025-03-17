(ns cn.academy.block.matrix-capability
  (:require [cn.academy.block.matrix-network-cap :as network-cap]
            [cn.academy.block.matrix-state :as state])
  (:import [net.minecraftforge.common.capabilities Capability ICapabilityProvider]
           [net.minecraftforge.common.util LazyOptional NonNullSupplier]
           [net.minecraft.util Direction]))

(defprotocol IMatrixCapability
  (get-capability [this cap dir])
  (invalidate-caps [this])
  (register-capability [this]))

(defrecord MatrixCapabilityProvider [matrix state caps-atom]
  IMatrixCapability
  (get-capability [_ cap dir]
    (when (= (.getName cap) "academy:wireless_matrix")
      (or (get @caps-atom :matrix)
          (let [handler (network-cap/create-network-handler matrix)]
            (swap! caps-atom assoc :matrix 
                   (LazyOptional/of 
                     (reify NonNullSupplier
                       (get [_] handler))))
            (get @caps-atom :matrix)))))
  
  (invalidate-caps [_]
    (some-> (get @caps-atom :matrix) .invalidate)
    (reset! caps-atom {}))
  
  (register-capability [_]
    (Capability$Builder. "academy:wireless_matrix"
                        network-cap/IMatrixNetworkCapability
                        #(network-cap/create-network-handler nil))))

(defn create-capability-provider [matrix state]
  (->MatrixCapabilityProvider matrix state (atom {})))