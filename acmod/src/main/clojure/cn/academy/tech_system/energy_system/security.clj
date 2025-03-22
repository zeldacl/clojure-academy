(ns cn.academy.tech-system.energy-system.security
  (:require [clojure.tools.logging :as log]
            [mcmod.nbt :as nbt]
            [cn.academy.tech-system.energy-system.config :as config]))

;; Unified security state
(def ^:private security-state
  (atom {:access-logs {}
         :banned-players #{}
         :access-rates {}
         :blocked-nodes #{}
         :last-cleanup nil}))

(defprotocol ISecurityPolicy
  (check-access [this node player pwd] "Check if player has access to node")
  (log-access-attempt [this node player success] "Log an access attempt")
  (add-to-blacklist [this player] "Add player to blacklist")
  (remove-from-blacklist [this player] "Remove player from blacklist"))

;; Node access rate-limiting and blocking

(defn- record-access-attempt! 
  "Records an access attempt for a node, tracking success/failure"
  [node-id success?]
  (let [now (System/currentTimeMillis)]
    (swap! security-state update-in [:access-logs node-id] 
           (fnil conj []) {:timestamp now :success success?})
    (swap! security-state update-in [:access-rates node-id]
           #(conj (or % []) {:timestamp now :success success?}))))

(defn- cleanup-old-attempts!
  "Remove attempts older than the block duration"
  []
  (let [now (System/currentTimeMillis)
        block-duration (config/get-config [:security :block-duration] 300000)
        cleanup-threshold (- now block-duration)
        last-cleanup (:last-cleanup @security-state)]
    (when (or (nil? last-cleanup) (< last-cleanup (- now 60000)))
      (swap! security-state update :access-rates
             (fn [rates]
               (reduce-kv
                (fn [m k v]
                  (assoc m k (filterv #(> (:timestamp %) cleanup-threshold) v)))
                {} rates)))
      ;; Auto-unblock nodes that have been blocked for longer than the duration
      (let [blocked (:blocked-nodes @security-state)
            access-rates (:access-rates @security-state)
            to-unblock (filter (fn [node-id]
                                (let [attempts (get access-rates node-id)
                                      latest (when (seq attempts)
                                               (apply max (map :timestamp attempts)))]
                                  (or (nil? latest)
                                      (< latest cleanup-threshold))))
                              blocked)]
        (when (seq to-unblock)
          (swap! security-state update :blocked-nodes
                 #(apply disj % to-unblock))
          (log/info "Auto-unblocked nodes:" (pr-str to-unblock))))
      (swap! security-state assoc :last-cleanup now))))

(defn- check-rate-limit 
  "Check if a node has exceeded the allowed failure rate"
  [node-id]
  (let [now (System/currentTimeMillis)
        block-duration (config/get-config [:security :block-duration] 300000)
        max-attempts (config/get-config [:security :max-attempts] 5)
        attempts (get-in @security-state [:access-rates node-id])
        recent-attempts (filter #(> (:timestamp %) (- now block-duration)) attempts)
        failed-attempts (remove :success recent-attempts)]
    (< (count failed-attempts) max-attempts)))

(defn verify-access! 
  "Verify access to a node with password, applying rate limiting logic"
  [node user password]
  (let [node-id (:id node)]
    (cleanup-old-attempts!)
    (if (contains? (:blocked-nodes @security-state) node-id)
      false
      (if (check-rate-limit node-id)
        (let [success? (= (:password-hash node) (hash password))]
          (record-access-attempt! node-id success?)
          (when-not success?
            (when (not (check-rate-limit node-id))
              (swap! security-state update :blocked-nodes conj node-id)
              (log/warn "Node" node-id "blocked due to too many failed attempts")))
          success?)
        false))))

(defn unblock-node! 
  "Manually unblock a node"
  [node-id]
  (swap! security-state update :blocked-nodes disj node-id))

(defn get-blocked-nodes 
  "Get all currently blocked nodes"
  []
  (:blocked-nodes @security-state))

;; Player-based security implementation

(defrecord SecurityManager [state-atom]
  ISecurityPolicy
  (check-access [_ node player pwd]
    (let [player-id (.getUniqueID player)]
      (when-not (contains? (:banned-players @state-atom) player-id)
        (verify-access! node player pwd))))
  
  (log-access-attempt [_ node player success]
    (let [player-id (.getUniqueID player)
          node-id (:id node)
          timestamp (System/currentTimeMillis)
          attempt {:player player-id
                  :timestamp timestamp
                  :success success}]
      (swap! state-atom update-in [:access-logs node-id] 
             (fnil conj []) attempt)))
  
  (add-to-blacklist [_ player]
    (swap! state-atom update :banned-players conj (.getUniqueID player)))
  
  (remove-from-blacklist [_ player]
    (swap! state-atom update :banned-players disj (.getUniqueID player))))

(def manager 
  "The default security manager instance"
  (->SecurityManager security-state))

(defn create-security-manager 
  "Create a new security manager instance with a fresh state"
  []
  (->SecurityManager (atom {:access-logs {}
                          :banned-players #{}
                          :access-rates {}
                          :blocked-nodes #{}})))

;; Persistence

(defn save-security-state! 
  "Save security state to NBT compound tag"
  [compound]
  (let [state @security-state
        banned-tag (nbt/create-list)
        blocked-tag (nbt/create-list)]
    
    ;; Save banned players
    (doseq [player-id (:banned-players state)]
      (nbt/add-uuid banned-tag player-id))
    (nbt/put-tag compound "banned_players" banned-tag)
    
    ;; Save blocked nodes
    (doseq [node-id (:blocked-nodes state)]
      (nbt/add-string blocked-tag node-id))
    (nbt/put-tag compound "blocked_nodes" blocked-tag)
    
    ;; Save access logs summary
    (nbt/put-compound compound "access_logs"
      (reduce-kv (fn [c k v]
                  (doto c
                    (nbt/put-long-array k 
                      (mapv :timestamp v)))) 
                (nbt/create-compound)
                (:access-logs state)))))

(defn load-security-state! 
  "Load security state from NBT compound tag"
  [compound]
  (let [banned-players (when (nbt/contains? compound "banned_players")
                        (set (nbt/get-uuid-list compound "banned_players")))
        blocked-nodes (when (nbt/contains? compound "blocked_nodes") 
                        (into #{} (map nbt/get-string (nbt/get-list compound "blocked_nodes"))))]
    (swap! security-state 
           #(cond-> %
              banned-players (assoc :banned-players banned-players)
              blocked-nodes (assoc :blocked-nodes blocked-nodes)))))

(defn init!
  "Initialize the security system"
  []
  (let [check-interval (config/get-config [:security :cleanup-interval] 60000)
        cleanup-thread (Thread.
                        (fn []
                          (try
                            (while true
                              (cleanup-old-attempts!)
                              (Thread/sleep check-interval))
                            (catch InterruptedException _))))]
    (.setDaemon cleanup-thread true)
    (.start cleanup-thread)
    (log/info "Security system initialized")))