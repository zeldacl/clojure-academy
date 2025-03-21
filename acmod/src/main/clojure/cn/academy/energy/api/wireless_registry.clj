(ns cn.academy.energy.api.wireless-registry
  (:require [cn.academy.energy.capability.wireless-node-capability :as node-cap]
            [cn.academy.energy.impl.wireless-system :as system]
            [cn.academy.energy.capability.wireless-capability-provider :as cap-provider]
            [cn.academy.energy.capability.wireless-capability-handler :as cap-handler]
            [mcmod.event :as event]))

(defn register-capabilities! []
  (node-cap/register!)
  (system/init!))

(defn register-event-handlers! [event-bus]
  (let [sys (system/get-instance)]
    (event/register-handler 
      {:setup (fn [_] (register-capabilities!))})))

(defn create-node-provider [node]
  (cap-provider/create-node-provider node))

(defn create-matrix-provider [matrix]
  (cap-provider/create-matrix-provider matrix))

;; Register for setup event using mcmod abstraction
(event/on-common-setup register-capabilities!)