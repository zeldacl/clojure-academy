(ns cn.academy.blocks.block-matrix.network
  "Network management for the wireless matrix block.
   Handles connections to the energy system network."
  (:require [cn.academy.blocks.block-matrix.protocols :refer [IMatrixNetwork]]
            [cn.academy.blocks.block-matrix.utils :as utils]
            [cn.academy.blocks.block-matrix.config :as config]
            [cn.academy.tech-system.energy-system.api :as energy-api]
            [clojure.tools.logging :as log]))

;; Network management implementation
(defrecord MatrixNetwork [config state energy position network-atom]
  IMatrixNetwork
  (join-network [this network-id password]
    (utils/with-error-handling "Error joining network"
      (when (and network-id 
                 (or (empty? password) 
                     ((:validate-password state) password)))
        (let [result (energy-api/join-network! 
                       network-id 
                       position 
                       {:owner ((:get-owner state))
                        :max-energy ((:get-energy-capacity energy))
                        :current-energy ((:get-energy energy))
                        :range (get-network-range this)
                        :transfer-rate ((:get-transfer-rate energy))
                        :password password})]
          (when result
            (swap! network-atom assoc 
                   :network-id network-id
                   :connected true)
            true)))))
  
  (leave-network [this]
    (utils/with-error-handling "Error leaving network"
      (when-let [network-id (get-network-id this)]
        (energy-api/leave-network! network-id position)
        (swap! network-atom assoc 
               :network-id nil
               :connected false)
        true)))
  
  (is-connected? [_]
    (:connected @network-atom))
  
  (get-network-id [_]
    (:network-id @network-atom))
  
  (get-network-range [_]
    (let [core-level ((:get-core-level state))]
      (utils/calculate-transfer-rate
        (config/get-config-value config :network :base-range)
        (config/get-config-value config :network :range-multiplier)
        core-level)))
  
  (get-connected-nodes [this]
    (when-let [network-id (get-network-id this)]
      (energy-api/get-network-nodes network-id)))
  
  (can-connect-node? [this node-pos]
    (let [range (get-network-range this)
          distance (utils/calc-distance position node-pos)]
      (and ((:is-formed? state))
           ((:is-active? state))
           (<= distance range)))))

;; Network event handling
(defn handle-network-tick!
  "Handle network-related updates on tick"
  [network]
  (utils/with-error-handling "Error in network tick"
    (when (and ((:is-formed? (:state network))) 
               ((:is-active? (:state network))))
      (let [connected? (is-connected? network)
            network-id (get-network-id network)]
        
        ;; Update connection state if needed
        (when (and (not connected?) (nil? network-id))
          (let [auto-network (or (get-in @(:config network) [:network :auto-network])
                                 "public")]
            (when-not (empty? auto-network)
              (join-network network auto-network ""))))))))

;; Network stats and utility functions
(defn get-node-count
  "Get count of nodes connected to the matrix"
  [network]
  (count (or (get-connected-nodes network) [])))

(defn get-network-bandwidth
  "Get total available network bandwidth"
  [network]
  (let [core-level ((:get-core-level (:state network)))]
    (* (config/get-config-value (:config network) :network :base-bandwidth) 
       core-level)))

(defn get-nodes-in-range
  "Get all nodes in range of this matrix"
  [network world]
  (let [range (get-network-range network)
        pos (:position network)]
    (energy-api/get-nodes-in-range world pos range)))

;; Factory function
(defn create-network
  "Create a new matrix network manager"
  [config state energy position]
  (->MatrixNetwork config state energy position 
                  (atom {:network-id nil
                         :connected false})))

;; Serialization helpers
(defn get-network-data
  "Get network data for serialization"
  [network]
  {:network-id (get-network-id network)
   :connected (is-connected? network)})

(defn load-network-data!
  "Load network data from serialized form"
  [network data]
  (reset! (:network-atom network) data))