(ns cn.academy.tech-system.energy-system.persistence
  (:require [cn.academy.tech-system.energy-system.network.state :as network-state]
            [cn.academy.tech-system.energy-system.config :as config]
            [mcmod.nbt :as nbt]
            [clojure.tools.logging :as log]))

(defprotocol IPersistable
  (save-to-nbt! [this nbt-tag])
  (load-from-nbt! [this nbt-tag]))

(defn- save-network! [network-id network nbt]
  (let [network-tag (nbt/create-compound)]
    ;; Save basic network info
    (nbt/put-string network-tag "id" network-id)
    (nbt/put-string network-tag "type" (name (:type network)))
    
    ;; Save node list
    (let [nodes-tag (nbt/create-list)]
      (doseq [node-id (:nodes network)]
        (nbt/add-string nodes-tag node-id))
      (nbt/put-tag network-tag "nodes" nodes-tag))
    
    ;; Save network properties
    (when-let [props (:properties network)]
      (let [props-tag (nbt/create-compound)]
        (doseq [[k v] props]
          (nbt/put-any props-tag (name k) v))
        (nbt/put-tag network-tag "properties" props-tag)))
    
    network-tag))

(defn- load-network! [network-tag]
  (let [network-id (nbt/get-string network-tag "id")
        network-type (keyword (nbt/get-string network-tag "type"))
        nodes (into #{} (map nbt/get-string (nbt/get-list network-tag "nodes")))
        properties (when-let [props-tag (nbt/get-compound network-tag "properties")]
                    (into {} (map (fn [[k v]] 
                                  [(keyword k) (nbt/get-any props-tag k)])
                                (.get-keys props-tag))))]
    {:id network-id
     :type network-type
     :nodes nodes
     :properties properties}))

(defn save-all! [world]
  (try
    (let [save-dir (mcmod.world/get-save-directory world)
          networks-file (io/file save-dir "energy_networks.dat")
          nbt (nbt/create-compound)]
      
      ;; Save network states
      (let [networks-tag (nbt/create-list)]
        (doseq [[network-id network] (network-state/get-all-networks)]
          (nbt/add-compound networks-tag 
                           (save-network! network-id network nbt)))
        (nbt/put-tag nbt "networks" networks-tag))
      
      ;; Write to file
      (nbt/write! nbt networks-file)
      (log/info "Saved" (count (network-state/get-all-networks)) "networks"))
    (catch Exception e
      (log/error "Failed to save network state:" (.getMessage e)))))

(defn load-all! [world]
  (try
    (let [save-dir (mcmod.world/get-save-directory world)
          networks-file (io/file save-dir "energy_networks.dat")]
      (when (.exists networks-file)
        (let [nbt (nbt/read! networks-file)]
          (when-let [networks-tag (nbt/get-list nbt "networks")]
            (doseq [network-tag networks-tag]
              (let [network (load-network! network-tag)]
                (network-state/register-network! (:id network) network)))
            (log/info "Loaded" (count networks-tag) "networks")))))
    (catch Exception e
      (log/error "Failed to load network state:" (.getMessage e)))))

(defn init! []
  (let [save-interval (config/get-config [:storage :save-interval] 300000)]
    (mcmod.scheduler/schedule-recurring 
      save-interval
      #(when-let [world (mcmod.world/get-current-world)]
         (save-all! world)))
    (log/info "Network persistence system initialized")))