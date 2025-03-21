(ns cn.academy.tech-system.energy-system.transfer
  (:require [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.network.optimization :as optimization]
            [mcmod.capabilities :as cap]
            [clojure.tools.logging :as log]))

;; Network packet protocols
(defprotocol INetworkPacket
  (encode [this buffer])
  (decode [this buffer])
  (handle [this world]))

(defrecord EnergyTransferPacket [source-id target-id amount]
  INetworkPacket
  (encode [_ buffer]
    (.writeString buffer source-id)
    (.writeString buffer target-id)
    (.writeDouble buffer amount))
  
  (decode [_ buffer]
    {:source-id (.readString buffer)
     :target-id (.readString buffer)
     :amount (.readDouble buffer)})
  
  (handle [this world]
    (when-let [source (network/get-node source-id)]
      (when-let [target (network/get-node target-id)]
        (transfer-energy! source target amount)))))

;; Existing energy transfer functions
(defn transfer-energy!
  "Transfer energy between two tile entities"
  [source-te target-te max-transfer]
  (when-let [source-storage (cap/get-capability source-te "forge:energy" nil)]
    (when-let [target-storage (cap/get-capability target-te "forge:energy" nil)]
      (let [energy-available (cap/extract-energy source-storage max-transfer true)
            energy-accepted (cap/receive-energy target-storage energy-available true)]
        (when (pos? energy-accepted)
          (cap/extract-energy source-storage energy-accepted false)
          (cap/receive-energy target-storage energy-accepted false)
          ;; Track transfer for optimization
          (optimization/track-bandwidth! (:id source-te) energy-accepted)
          energy-accepted)))))

(defn transfer-to-network!
  "Transfer energy from a tile entity to its wireless network"
  [tile-entity amount]
  (when-let [network-id (network/get-node-network (:id tile-entity))]
    (when-let [network (network/get-network network-id)]
      (let [nodes (network/get-nodes network)
            active-nodes (filter #(wireless/can-receive? %) nodes)
            node-count (count active-nodes)]
        (when (pos? node-count)
          (let [per-node (quot amount node-count)]
            (reduce + (map #(transfer-energy! tile-entity % per-node) active-nodes))))))))

(defn extract-from-network!
  "Extract energy from a wireless network to a tile entity"
  [tile-entity amount]
  (when-let [network-id (network/get-node-network (:id tile-entity))]
    (when-let [network (network/get-network network-id)]
      (let [nodes (network/get-nodes network)
            active-nodes (filter #(wireless/can-extract? %) nodes)
            total-available (reduce + (map #(cap/get-energy-stored %) active-nodes))
            actual-extract (min amount total-available)]
        (when (pos? actual-extract)
          (let [per-node (quot actual-extract (count active-nodes))]
            (reduce + (map #(transfer-energy! % tile-entity per-node) active-nodes))))))))

;; Network initialization
(defn init-network! []
  (network/register-packet! :energy-transfer ->EnergyTransferPacket)
  (optimization/init!))