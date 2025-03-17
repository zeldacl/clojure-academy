(ns cn.academy.block.matrix-capability
  (:require [cn.academy.block.matrix-network-cap :as network-cap]
            [cn.academy.block.matrix-state :as state])
  (:import [net.minecraftforge.common.capabilities Capability CapabilityInject ICapabilityProvider]
           [net.minecraft.util EnumFacing]))

(defprotocol IMatrixCapability
  (get-capability [this cap facing])
  (has-capability [this cap facing])
  (register-capability [this]))

(defrecord MatrixCapabilityProvider [matrix state caps-atom]
  IMatrixCapability
  (has-capability [_ cap facing]
    (= (.getName cap) "academy:wireless_matrix"))
  
  (get-capability [_ cap facing]
    (when (has-capability cap facing)
      (or (get @caps-atom :matrix)
          (let [handler (network-cap/create-network-handler matrix)]
            (swap! caps-atom assoc :matrix handler)
            handler))))
  
  (register-capability [_]
    (.register (CapabilityManager/INSTANCE)
               network-cap/IMatrixNetworkCapability
               (reify Callable
                 (call [_]
                   (network-cap/create-network-handler nil)))
               #())))

(defn create-capability-provider [matrix state]
  (->MatrixCapabilityProvider matrix state (atom {})))