(ns cn.academy.tech-system.energy-system.security
  (:require [clojure.tools.logging :as log]
            [mcmod.nbt :as nbt]))

(def ^:private security-state
  (atom {:access-logs {}
         :banned-players #{}}))

(defprotocol ISecurityPolicy
  (check-access [this node player pwd] "Check if player has access to node")
  (log-access-attempt [this node player success] "Log an access attempt")
  (add-to-blacklist [this player] "Add player to blacklist")
  (remove-from-blacklist [this player] "Remove player from blacklist"))

(defrecord SecurityManager [state-atom]
  ISecurityPolicy
  (check-access [_ node player pwd]
    (let [player-id (.getUniqueID player)]
      (when-not (contains? (:banned-players @state-atom) player-id)
        (if (empty? (:password-hash node))
          true
          (= (:password-hash node) (hash pwd))))))
  
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

(defn create-security-manager []
  (->SecurityManager (atom {:access-logs {}
                          :banned-players #{}})))

(defn save-security-state! [compound]
  (let [state @security-state]
    (doto compound
      (nbt/put-compound "access_logs"
        (reduce-kv (fn [c k v]
                    (doto c
                      (nbt/put-long-array k 
                        (mapv :timestamp v)))) 
                  (nbt/create-compound)
                  (:access-logs state)))
      (nbt/put-uuid-list "banned_players" 
        (:banned-players state)))))

(defn load-security-state! [compound]
  (when (nbt/contains? compound "banned_players")
    (reset! security-state
            {:access-logs {}
             :banned-players (set (nbt/get-uuid-list compound "banned_players"))})))