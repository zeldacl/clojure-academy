(ns cn.academy.tech-system.energy-system.security.manager
  (:require [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [mcmod.nbt :as nbt]
            [clojure.tools.logging :as log]))

(def ^:private security-state
  (atom {:access-rates {}
         :blocked-nodes #{}
         :max-attempts 5
         :block-duration 300000})) ; 5 minutes in ms

(defn- record-access-attempt! [node-id success?]
  (let [now (System/currentTimeMillis)]
    (swap! security-state update-in [:access-rates node-id]
           #(conj (or % []) {:timestamp now :success success?}))))

(defn- check-rate-limit [node-id]
  (let [now (System/currentTimeMillis)
        cooldown (get-in @security-state [:block-duration])
        attempts (get-in @security-state [:access-rates node-id])
        recent-attempts (filter #(> (:timestamp %) (- now cooldown)) attempts)
        failed-attempts (remove :success recent-attempts)]
    (< (count failed-attempts) (get-in @security-state [:max-attempts]))))

(defn verify-access! [node user password]
  (let [node-id (:id node)]
    (if (contains? (:blocked-nodes @security-state) node-id)
      false
      (if (check-rate-limit node-id)
        (let [success? (wireless/verify-password node password)]
          (record-access-attempt! node-id success?)
          (when-not success?
            (when (not (check-rate-limit node-id))
              (swap! security-state update :blocked-nodes conj node-id)
              (log/warn "Node" node-id "blocked due to too many failed attempts")))
          success?)
        false))))

(defn unblock-node! [node-id]
  (swap! security-state update :blocked-nodes disj node-id))

(defn get-blocked-nodes []
  (:blocked-nodes @security-state))

(defn save-state! [nbt]
  (let [blocked-tag (nbt/create-list)]
    (doseq [node-id (get-blocked-nodes)]
      (nbt/add-string blocked-tag node-id))
    (nbt/put-tag nbt "blocked_nodes" blocked-tag)))

(defn load-state! [nbt]
  (when-let [blocked-tag (nbt/get-list nbt "blocked_nodes")]
    (let [blocked (into #{} (map nbt/get-string blocked-tag))]
      (swap! security-state assoc :blocked-nodes blocked))))