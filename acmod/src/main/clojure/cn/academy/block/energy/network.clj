(ns cn.academy.block.energy.network
  (:require [mcmod.protocols :refer [IEnergyNetwork IEnergyNode IEnergyStorage]]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Network state tracking
(def network-state
  (atom {:networks {}
         :node-mappings {}}))

;; Energy network implementation
(defrecord EnergyNetwork [id state-atom]
  IEnergyNetwork
  (add-node! [_ node]
    (when (satisfies? IEnergyNode node)
      (swap! state-atom update :nodes conj node)
      (swap! network-state assoc-in [:node-mappings (:id node)] id)))
  
  (remove-node! [_ node]
    (swap! state-atom update :nodes disj node)
    (swap! network-state update :node-mappings dissoc (:id node)))
  
  (get-nodes [_]
    (:nodes @state-atom))
  
  (distribute-energy! [this]
    (let [nodes (.get-nodes this)
          total-energy (reduce + (map #(.get-energy-stored %) nodes))
          target-energy (/ total-energy (count nodes))]
      (doseq [node nodes]
        (let [current (.get-energy-stored node)
              diff (- target-energy current)]
          (if (pos? diff)
            (.receive-energy node diff false)
            (.extract-energy node (- diff) false))))))
            
  (get-total-energy [this]
    (reduce + (map #(.get-energy-stored %) (.get-nodes this))))
  
  (get-total-capacity [this]
    (reduce + (map #(.get-max-energy-stored %) (.get-nodes this)))))

;; Energy node implementation  
(defrecord EnergyNode [id pos network storage]
  IEnergyNode
  (get-network [_]
    network)
  
  (set-network! [this new-network]
    (when-let [old-network network]
      (.remove-node! old-network this))
    (set! network new-network)
    (when new-network
      (.add-node! new-network this)))
  
  (get-position [_]
    pos)
  
  IEnergyStorage
  (get-energy-stored [_]
    (.get-energy-stored storage))
  
  (get-max-energy-stored [_]
    (.get-max-energy-stored storage))
  
  (receive-energy [_ amount simulate]
    (.receive-energy storage amount simulate))
  
  (extract-energy [_ amount simulate]
    (.extract-energy storage amount simulate)))

;; Network management
(defn create-network! []
  (let [id (str (random-uuid))
        network (->EnergyNetwork id (atom {:nodes #{}}))]
    (swap! network-state assoc-in [:networks id] network)
    network))

(defn get-network [id]
  (get-in @network-state [:networks id]))

(defn create-node! [pos storage]
  (let [id (str (random-uuid))
        node (->EnergyNode id pos nil storage)]
    node))

;; Network operations
(defn merge-networks! [network1 network2]
  (let [nodes (.get-nodes network2)]
    (doseq [node nodes]
      (.set-network! node network1))
    (swap! network-state update :networks dissoc (:id network2))))

(defn split-network! [network nodes]
  (let [new-network (create-network!)]
    (doseq [node nodes]
      (.set-network! node new-network))))

;; Network monitoring
(defn validate-networks! []
  (doseq [[id network] (:networks @network-state)]
    (error/with-safe-execution id :network
      (let [nodes (.get-nodes network)]
        (when (empty? nodes)
          (swap! network-state update :networks dissoc id))))))