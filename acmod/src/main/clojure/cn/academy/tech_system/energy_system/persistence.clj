(ns cn.academy.tech-system.energy-system.persistence
  (:require [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.security :as security]
            [mcmod.nbt :as nbt]
            [mcmod.world :as world]
            [clojure.tools.logging :as log]))

(defprotocol INetworkStorage
  (save-network! [this world net-id])
  (load-network! [this world net-id])
  (save-all! [this world])
  (load-all! [this world]))

(defrecord NetworkStorage []
  INetworkStorage
  (save-network! [_ world net-id]
    (when-let [network (network/get-network net-id)]
      (let [data (nbt/create-compound)]
        ;; Save basic network info
        (nbt/put-string data "id" net-id)
        (nbt/put-int data "node_count" (count (network/get-nodes network)))
        
        ;; Save node data
        (let [nodes-tag (nbt/create-list)]
          (doseq [node (network/get-nodes network)]
            (let [node-tag (nbt/create-compound)]
              (nbt/put-string node-tag "id" (:id node))
              (nbt/put-int node-tag "pos_x" (get-in node [:pos :x]))
              (nbt/put-int node-tag "pos_y" (get-in node [:pos :y]))
              (nbt/put-int node-tag "pos_z" (get-in node [:pos :z]))
              (nbt/put-double node-tag "energy" (network/get-energy node))
              (.add nodes-tag node-tag)))
          (nbt/put-list data "nodes" nodes-tag))
        
        ;; Save to world data
        (world/set-world-data! world 
                              (str "energy_network_" net-id)
                              data))))
  
  (load-network! [_ world net-id]
    (when-let [data (world/get-world-data world 
                                         (str "energy_network_" net-id))]
      (let [network (network/create-network!)]
        ;; Load nodes
        (when-let [nodes-tag (nbt/get-list data "nodes")]
          (doseq [node-tag nodes-tag]
            (let [node-id (nbt/get-string node-tag "id")
                  pos {:x (nbt/get-int node-tag "pos_x")
                      :y (nbt/get-int node-tag "pos_y")
                      :z (nbt/get-int node-tag "pos_z")}
                  energy (nbt/get-double node-tag "energy")]
              (when-let [te (world/get-tile-entity world pos)]
                (network/add-node! network te)))))
        network)))
  
  (save-all! [this world]
    (let [networks (network/get-all-networks)]
      (doseq [net-id (keys networks)]
        (save-network! this world net-id))
      ;; Save security state
      (let [sec-data (nbt/create-compound)]
        (security/save-security-state! sec-data)
        (world/set-world-data! world "energy_security" sec-data))))
  
  (load-all! [this world]
    ;; Load security state first
    (when-let [sec-data (world/get-world-data world "energy_security")]
      (security/load-security-state! sec-data))
    
    ;; Load networks
    (doseq [data-key (world/get-all-data-keys world)]
      (when (.startsWith data-key "energy_network_")
        (let [net-id (.substring data-key 14)]
          (load-network! this world net-id))))))

(def storage (->NetworkStorage))

(defn init! []
  ;; Register world save handler
  (world/register-save-handler 
    (fn [world]
      (save-all! storage world)))
  
  ;; Register world load handler
  (world/register-load-handler
    (fn [world]
      (load-all! storage world))))