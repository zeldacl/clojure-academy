(ns cn.academy.block.energy.adapter
  (:require [cn.academy.tech-system.energy-system.network.distribution :as distribution]
            [cn.academy.tech-system.energy-system.registry :as energy-registry]
            [mcmod.protocols :refer [IEnergyStorage]]
            [clojure.tools.logging :as log]))

;; Adapter for block-based energy nodes
(defn create-energy-node! [block storage]
  (let [pos (mcmod.block/get-pos block)
        node (distribution/create-node! pos storage)]
    ;; Store node reference in block for future lookup
    (swap! (:state block) assoc :energy-node node)
    node))

(defn get-node-from-block [block]
  (get-in @(:state block) [:energy-node]))

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
             (map #(mcmod.block/get-block-at-pos (.get-position %)))
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
            ;; For multiple nodes, rebuild network connections
            (let [new-network (distribution/create-network!)]
              (doseq [remaining-node remaining-nodes]
                (.set-network! remaining-node new-network)))))))))

(defn init! []
  (log/info "Block energy adapter initialized"))