(ns cn.academy.tech-system.energy-system.persistence
  (:require [cn.academy.tech-system.energy-system.network.state :as network-state]
            [cn.academy.tech-system.energy-system.config :as config]
            [cn.academy.tech-system.energy-system.security :as security]
            [cn.academy.tech-system.energy-system.monitoring :as monitoring]
            [mcmod.nbt :as nbt]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

(defprotocol IPersistable
  "Protocol for components that need to save/load state"
  (save-to-nbt! [this nbt-tag] "Save component state to NBT")
  (load-from-nbt! [this nbt-tag] "Load component state from NBT"))

;; Registry of persistable components
(def ^:private persistable-components (atom {}))

(defn register-persistable!
  "Register a component that needs to be persisted"
  [id component]
  (swap! persistable-components assoc id component))

;; Network persistence functions
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
          state-file (io/file save-dir "energy_system_state.dat")
          networks-nbt (nbt/create-compound)
          state-nbt (nbt/create-compound)]
      
      ;; Save network states
      (let [networks-tag (nbt/create-list)]
        (doseq [[network-id network] (network-state/get-all-networks)]
          (nbt/add-compound networks-tag 
                           (save-network! network-id network networks-nbt)))
        (nbt/put-tag networks-nbt "networks" networks-tag))
      
      ;; Write networks to file
      (nbt/write! networks-nbt networks-file)
      (log/info "Saved" (count (network-state/get-all-networks)) "networks")
      
      ;; Save other component states
      (doseq [[component-id component] @persistable-components]
        (let [component-tag (nbt/create-compound)]
          (try
            (save-to-nbt! component component-tag)
            (nbt/put-tag state-nbt (name component-id) component-tag)
            (catch Exception e
              (log/error "Failed to save component" component-id ":" (.getMessage e))))))
      
      ;; Save security state
      (let [security-tag (nbt/create-compound)]
        (security/save-security-state! security-tag)
        (nbt/put-tag state-nbt "security" security-tag))
      
      ;; Write state to file
      (nbt/write! state-nbt state-file)
      (log/info "Saved system state for" (inc (count @persistable-components)) "components"))
    (catch Exception e
      (log/error "Failed to save system state:" (.getMessage e)))))

(defn load-all! [world]
  (try
    (let [save-dir (mcmod.world/get-save-directory world)
          networks-file (io/file save-dir "energy_networks.dat")
          state-file (io/file save-dir "energy_system_state.dat")]
      
      ;; Load networks
      (when (.exists networks-file)
        (let [nbt (nbt/read! networks-file)]
          (when-let [networks-tag (nbt/get-list nbt "networks")]
            (doseq [network-tag networks-tag]
              (let [network (load-network! network-tag)]
                (network-state/register-network! (:id network) network)))
            (log/info "Loaded" (count networks-tag) "networks"))))
      
      ;; Load component states
      (when (.exists state-file)
        (let [state-nbt (nbt/read! state-file)]
          ;; Load security state
          (when (nbt/contains? state-nbt "security")
            (security/load-security-state! (nbt/get-compound state-nbt "security")))
          
          ;; Load other component states
          (doseq [[component-id component] @persistable-components]
            (let [component-key (name component-id)]
              (when (nbt/contains? state-nbt component-key)
                (try
                  (load-from-nbt! component (nbt/get-compound state-nbt component-key))
                  (catch Exception e
                    (log/error "Failed to load component" component-id ":" (.getMessage e))))))))))
    (catch Exception e
      (log/error "Failed to load system state:" (.getMessage e)))))

(defn create-backup! [world]
  (try
    (let [save-dir (mcmod.world/get-save-directory world)
          networks-file (io/file save-dir "energy_networks.dat")
          state-file (io/file save-dir "energy_system_state.dat")
          backup-count (config/get-config [:storage :backup-count] 3)
          timestamp (System/currentTimeMillis)]
      
      (when (.exists networks-file)
        (let [backup-file (io/file save-dir (str "energy_networks_" timestamp ".bak"))]
          (io/copy networks-file backup-file)
          (prune-backups save-dir "energy_networks_" backup-count)))
      
      (when (.exists state-file)
        (let [backup-file (io/file save-dir (str "energy_system_state_" timestamp ".bak"))]
          (io/copy state-file backup-file)
          (prune-backups save-dir "energy_system_state_" backup-count))))
    (catch Exception e
      (log/error "Failed to create backup:" (.getMessage e)))))

(defn- prune-backups [dir prefix max-count]
  (let [backup-files (->> (.listFiles dir)
                         (filter #(.isFile %))
                         (filter #(.startsWith (.getName %) prefix))
                         (filter #(.endsWith (.getName %) ".bak"))
                         (sort-by #(.lastModified %)))]
    (when (> (count backup-files) max-count)
      (doseq [file (take (- (count backup-files) max-count) backup-files)]
        (.delete file)))))

(defn init! []
  (let [save-interval (config/get-config [:storage :save-interval] 300000)
        backup-interval (* 6 save-interval)]
    
    ;; Schedule regular saving
    (mcmod.scheduler/schedule-recurring 
      save-interval
      #(when-let [world (mcmod.world/get-current-world)]
         (save-all! world)))
    
    ;; Schedule backups
    (mcmod.scheduler/schedule-recurring 
      backup-interval
      #(when-let [world (mcmod.world/get-current-world)]
         (create-backup! world)))
    
    (log/info "Persistence system initialized with save interval of" 
              (int (/ save-interval 1000)) "seconds")))