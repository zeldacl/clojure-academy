(ns cn.academy.tech-system.energy-system.events
  (:require [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.security :as security]
            [mcmod.event :as event]
            [mcmod.world :as world]
            [clojure.tools.logging :as log]))

;; Event handling for node placement/removal
(defn- on-node-placed [event]
  (let [{:keys [world pos node player]} event]
    (when (wireless/is-wireless-node? node)
      ;; Check for nearby networks
      (let [nearby-nodes (network/find-nearby-nodes world pos)]
        (if (seq nearby-nodes)
          ;; Join existing network
          (let [target-net (network/get-node-network (:id (first nearby-nodes)))]
            (network/add-node! target-net node)
            (log/info "Node joined existing network:" (:id target-net)))
          ;; Create new network
          (let [new-net (network/create-network!)]
            (network/add-node! new-net node)
            (log/info "Created new network for node:" (:id new-net))))))))

(defn- on-node-broken [event]
  (let [{:keys [world pos node]} event]
    (when (wireless/is-wireless-node? node)
      (when-let [net-id (network/get-node-network (:id node))]
        (when-let [net (network/get-network net-id)]
          ;; Remove node from network
          (network/remove-node! net node)
          ;; Check if network needs to be split
          (network/validate-network! net))))))

;; Event handling for node interaction
(defn- on-node-interact [event]
  (let [{:keys [world pos node player]} event]
    (when (wireless/is-wireless-node? node)
      (when-let [net-id (network/get-node-network (:id node))]
        (when-let [net (network/get-network net-id)]
          ;; Check security access
          (when (security/check-access security/manager node player nil)
            ;; Toggle node active state
            (wireless/toggle-active! node)
            (network/validate-network! net)))))))

;; Event handling for network updates
(defn- on-network-tick [event]
  (let [{:keys [world]} event]
    (doseq [[net-id net] (network/get-all-networks)]
      (try
        (network/balance-energy! net)
        (catch Exception e
          (log/error e "Error updating network:" net-id))))))

;; Register event handlers
(defn register-handlers! []
  (event/register-handler :node-placed #'on-node-placed)
  (event/register-handler :node-broken #'on-node-broken)
  (event/register-handler :node-interact #'on-node-interact)
  (event/register-handler :network-tick #'on-network-tick))