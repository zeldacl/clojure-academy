(ns cn.academy.events
  (:require [cn.academy.core :as core]
            [cn.academy.registry :as registry]
            [mcmod.network :as network]
            [mcmod.event :as event]))

(defn setup-network []
  (let [net-registry (network/create-registry)]
    ;; Register our energy sync packet
    (network/register-packet net-registry 
                          "energy_sync" 
                          cn.academy.network.energy_sync_packet.EnergySyncPacket
                          #(println "Energy synced:" %))))

(defn handle-common-setup [event]
  (setup-network)
  (core/init-mod))

;; Register event handler using mcmod event system
(event/register-handler 
  {:setup handle-common-setup})