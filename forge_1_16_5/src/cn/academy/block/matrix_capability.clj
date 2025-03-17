(ns cn.academy.block.matrix-capability
  (:require [cn.academy.block.matrix-network-cap :as network-cap]
            [cn.academy.block.matrix-state :as state])
  (:import [net.minecraftforge.common.capabilities Capability CapabilityInject ICapabilityProvider]
           [net.minecraftforge.common.util LazyOptional]
           [net.minecraft.util Direction]))

(def ^:private MATRIX_CAP (CapabilityInject net.minecraft.world.IWorldReader))

(defprotocol IMatrixCapability
  (get-capability [this cap dir])
  (invalidate-caps [this]))

(defrecord MatrixCapabilityProvider [matrix state caps-atom]
  IMatrixCapability
  (get-capability [_ cap dir]
    (when (= cap MATRIX_CAP)
      (or (get @caps-atom :matrix)
          (let [handler (network-cap/create-network-handler matrix)]
            (swap! caps-atom assoc :matrix 
                   (LazyOptional/of #(constantly handler)))
            (get @caps-atom :matrix)))))
  
  (invalidate-caps [_]
    (when-let [cap (get @caps-atom :matrix)]
      (.invalidate cap))
    (reset! caps-atom {})))

(defn create-capability-provider [matrix state]
  (->MatrixCapabilityProvider matrix state (atom {})))