(ns cn.academy.tech-system.energy-system.block.adapter
  (:require [cn.academy.tech-system.energy-system.block.node :as node]
            [cn.academy.tech-system.energy-system.block.matrix :as matrix]
            [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.network.distribution :as distribution]
            [mcmod.capabilities :as cap]
            [mcmod.block :as mcblock]
            [clojure.tools.logging :as log]))

(defprotocol IEnergyAdapter
  (to-wireless-node [this])
  (to-wireless-matrix [this])
  (to-energy-storage [this])
  (from-block [this block]))

(defrecord BlockEnergyAdapter []
  IEnergyAdapter
  (to-wireless-node [_ block]
    (when-let [energy-storage (cap/get-capability block "forge:energy")]
      (reify wireless/IWirelessNode
        (get-node-type [_] :block)
        (get-energy [_] (.get-energy-stored energy-storage))
        (get-max-energy [_] (.get-max-energy-stored energy-storage))
        (get-bandwidth [_] (get-in block [:config :energy :bandwidth] 1000.0))
        (get-range [_] (get-in block [:config :wireless :range] 16.0))
        (get-capacity [_] (get-in block [:config :wireless :max-connections] 4))
        (connect [this other] 
          (node/connect-to-network (.to-energy-storage this other)))
        (disconnect [this other]
          (node/disconnect-from-network (.to-energy-storage this other)))
        (can-connect? [_ other]
          (and (cap/has-capability? other "forge:energy")
               (.can-receive? energy-storage))))))
  
  (to-wireless-matrix [_ block]
    (when (cap/has-capability? block "academy:wireless_matrix")
      (let [matrix-id (str (random-uuid))
            config {:limits {:max-nodes 16
                           :range 32}
                   :energy {:transfer-rate 2000}}]
        (matrix/create-matrix matrix-id :block config))))
  
  (to-energy-storage [_ block]
    (when-let [node-storage (node/create-energy-storage block)]
      (reify cap/IEnergyStorage
        (receive-energy [_ amount simulate]
          (.receive-energy node-storage nil amount simulate))
        (extract-energy [_ amount simulate]
          (.extract-energy node-storage amount simulate))
        (get-energy-stored [_]
          (.get-energy-stored node-storage))
        (get-max-energy-stored [_]
          (.get-max-energy-stored node-storage))
        (can-extract? [_]
          (.can-extract? node-storage))
        (can-receive? [_]
          (.can-receive? node-storage)))))
  
  (from-block [this block]
    (cond
      (cap/has-capability? block "forge:energy")
      (to-wireless-node this block)
      
      (cap/has-capability? block "academy:wireless_matrix")
      (to-wireless-matrix this block)
      
      :else nil)))

(def adapter (->BlockEnergyAdapter))

(defn get-node-from-block [block]
  (get-in @(:state block) [:energy-node]))

(defn create-energy-node! [block storage]
  (let [pos (mcblock/get-pos block)
        node (distribution/create-node! pos storage)]
    ;; Store node reference in block for future lookup
    (swap! (:state block) assoc :energy-node node)
    node))

(defn connect-nodes! [block1 block2]
  (let [node1 (get-node-from-block block1)
        node2 (get-node-from-block block2)]
    (when (and node1 node2)
      (let [network1 (.get-network node1)
            network2 (.get-network node2)]
        (cond
          ;; Both have networks, merge them
          (and network1 network2) 
          (when (not= network1 network2)
            (distribution/merge-networks! network1 network2))
          
          ;; Only node1 has network, add node2
          network1 
          (.set-network! node2 network1)
          
          ;; Only node2 has network, add node1
          network2 
          (.set-network! node1 network2)
          
          ;; Neither has network, create one
          :else
          (let [network (distribution/create-network!)]
            (.set-network! node1 network)
            (.set-network! node2 network)))))))

(defn disconnect-nodes! [block1 block2]
  (let [node1 (get-node-from-block block1)
        node2 (get-node-from-block block2)]
    (when (and node1 node2)
      (let [network (.get-network node1)]
        (when (and network (= network (.get-network node2)))
          ;; If these are the only two nodes, just disconnect
          (let [nodes (.get-nodes network)]
            (if (<= (count nodes) 2)
              (do
                (.set-network! node1 nil)
                (.set-network! node2 nil))
              ;; Otherwise, keep node1 in current network and create new for node2
              (let [new-network (distribution/create-network!)]
                (.set-network! node2 new-network)))))))))

(defn get-connected-blocks [block]
  (when-let [node (get-node-from-block block)]
    (when-let [network (.get-network node)]
      (let [nodes (.get-nodes network)]
        (->> nodes
             (filter #(not= % node))
             (map #(mcblock/get-block-at-pos (.get-position %)))
             (filter identity))))))

(defn on-block-destroyed [block]
  (when-let [node (get-node-from-block block)]
    (when-let [network (.get-network node)]
      (let [nodes (.get-nodes network)
            remaining-nodes (disj nodes node)]
        ;; Remove from network
        (.set-network! node nil)
        
        ;; If there are nodes left, ensure they stay connected
        (when (seq remaining-nodes)
          (if (= (count remaining-nodes) 1)
            ;; If only one node left, it doesn't need a network
            (.set-network! (first remaining-nodes) nil)
            ;; Otherwise, rebuild network connections
            (let [new-network (distribution/create-network!)]
              (doseq [remaining-node remaining-nodes]
                (.set-network! remaining-node new-network))))))))