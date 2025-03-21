(ns cn.academy.blocks.block_matrix.network
  (:require [cn.academy.blocks.block_matrix.config :as config]
            [cn.academy.blocks.block_matrix.state :as state]
            [cn.academy.tech-system.energy-system.network.distribution :as distribution]
            [clojure.tools.logging :as log]))

(defprotocol IMatrixNetwork
  (join-network [this network-id password])
  (leave-network [this])
  (get-network-id [this])
  (get-network-name [this])
  (get-nodes [this])
  (get-node-count [this])
  (is-owner? [this player-name])
  (can-join? [this player-name]))

(defrecord MatrixNetwork [matrix state network-atom]
  IMatrixNetwork
  (join-network [_ network-id password]
    (when-let [network (distribution/get-network network-id)]
      (swap! network-atom assoc :network-id network-id)
      (swap! network-atom assoc :network network)
      true))
  
  (leave-network [_]
    (swap! network-atom dissoc :network-id)
    (swap! network-atom dissoc :network)
    true)
  
  (get-network-id [_]
    (:network-id @network-atom))
  
  (get-network-name [_]
    (get-in @network-atom [:network-info :name] "Unnamed Network"))
  
  (get-nodes [_]
    (if-let [network (:network @network-atom)]
      (distribution/get-network-nodes (:network-id @network-atom))
      []))
  
  (get-node-count [this]
    (count (get-nodes this)))
  
  (is-owner? [_ player-name]
    (= (get-in @network-atom [:owner]) player-name))
  
  (can-join? [_ player-name]
    (let [whitelist (get-in @network-atom [:whitelist] #{})
          blacklist (get-in @network-atom [:blacklist] #{})]
      (and (not (contains? blacklist player-name))
           (or (empty? whitelist)
               (contains? whitelist player-name))))))

(defn create-network [matrix]
  (->MatrixNetwork matrix 
                  (:state matrix)
                  (atom {:owner nil
                         :whitelist #{}
                         :blacklist #{}})))