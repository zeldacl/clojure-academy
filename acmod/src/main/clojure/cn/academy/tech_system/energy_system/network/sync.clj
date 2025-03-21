(ns cn.academy.tech-system.energy-system.network.sync
  (:require [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.network.messaging :as msg]
            [mcmod.network :as net]
            [mcmod.world :as world]
            [clojure.tools.logging :as log]))

(def ^:private sync-channel 
  (net/create-channel "energy_network_sync"))

;; Message types for network synchronization
(defrecord NetworkUpdateMessage [net-id nodes]
  net/IMessage
  (write [_ buffer]
    (net/write-string buffer net-id)
    (net/write-int buffer (count nodes))
    (doseq [node nodes]
      (net/write-string buffer (:id node))
      (net/write-pos buffer (:pos node))
      (net/write-double buffer (:energy node))
      (net/write-boolean buffer (:active node))))
  
  (read [_ buffer]
    (->NetworkUpdateMessage
      (net/read-string buffer)
      (vec (repeatedly (net/read-int buffer)
             #(hash-map
                :id (net/read-string buffer)
                :pos (net/read-pos buffer)
                :energy (net/read-double buffer)
                :active (net/read-boolean buffer))))))
  
  (handle [this side]
    (when (= side :client)
      (when-let [net (network/get-network net-id)]
        (network/sync-nodes! net nodes)))))

(defn send-network-update!
  "Send network state update to clients"
  [network players]
  (let [nodes (network/get-nodes network)
        msg (->NetworkUpdateMessage
              (:id network)
              (map (fn [node]
                     {:id (:id node)
                      :pos (network/get-position node)
                      :energy (network/get-energy node)
                      :active (network/is-active? node)})
                   nodes))]
    (doseq [player players]
      (net/send-to! sync-channel msg player))))

(defn register-handlers! []
  (net/register-message! sync-channel "network_update" ->NetworkUpdateMessage)
  
  ;; Register periodic sync task
  (world/register-server-tick-handler
    (fn [world]
      ;; Sync each network's state every 20 ticks (1 second)
      (when (zero? (mod (world/get-total-world-time world) 20))
        (doseq [[net-id net] (network/get-all-networks)]
          (send-network-update! net (world/get-players world)))))))