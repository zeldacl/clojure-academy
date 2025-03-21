(ns cn.academy.tech-system.energy-system.api.wireless
  (:require [cn.academy.tech-system.energy-system.network.state :as network-state]
            [cn.academy.tech-system.energy-system.network.handler :as handler]
            [cn.academy.tech-system.energy-system.transfer :as transfer]
            [cn.academy.tech-system.energy-system.capability.wireless :as cap]
            [mcmod.nbt :as nbt]))

;; Network Management
(defn create-network! [matrix ssid password]
  (let [network-id (str (random-uuid))]
    (network-state/register-network! 
      network-id
      {:id network-id
       :matrix matrix
       :ssid ssid
       :password password
       :nodes #{}})
    (handler/send-message! :network-formed matrix [])
    network-id))

(defn destroy-network! [network-id]
  (when-let [{:keys [nodes]} (network-state/get-network network-id)]
    (doseq [node nodes]
      (handler/send-message! :network-broken node))
    (network-state/remove-network! network-id)))

;; Node Management  
(defn add-node! [network-id node]
  (when (cap/is-wireless-node? node)
    (network-state/add-node! network-id (:id node))
    (handler/send-message! :network-formed (get-in (network-state/get-network network-id) [:matrix]) [node])))

(defn remove-node! [network-id node]
  (network-state/remove-node! network-id (:id node))
  (handler/send-message! :network-broken node))

(defn get-node-network [node]
  (network-state/get-node-network (:id node)))

(defn get-network [network-id]
  (network-state/get-network network-id))

;; Energy Transfer
(defn transfer-energy! [source target amount]
  (transfer/transfer-energy! source target amount))

(defn transfer-to-network! [node amount]
  (transfer/transfer-to-network! node amount))

(defn extract-from-network! [node amount]
  (transfer/extract-from-network! node amount))

;; Node State
(defn get-node-energy [node]
  (when-let [cap (cap/get-wireless-capability node)]
    (cap/get-energy cap)))

(defn set-node-energy! [node amount]
  (when-let [cap (cap/get-wireless-capability node)]
    (cap/set-energy! cap amount)
    (handler/send-message! :update-energy node amount)))

(defn get-node-max-energy [node]
  (when-let [cap (cap/get-wireless-capability node)]
    (cap/get-max-energy cap)))

(defn get-node-range [node]
  (when-let [cap (cap/get-wireless-capability node)]
    (cap/get-range cap)))

(defn get-node-bandwidth [node]
  (when-let [cap (cap/get-wireless-capability node)]
    (cap/get-bandwidth cap)))