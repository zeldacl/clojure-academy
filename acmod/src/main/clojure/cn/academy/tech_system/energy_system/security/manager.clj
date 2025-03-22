(ns cn.academy.tech-system.energy-system.security.manager
  (:require [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.security :as security]
            [mcmod.nbt :as nbt]
            [clojure.tools.logging :as log]))

;; This is a compatibility layer for the new unified security system
;; All calls are now forwarded to the main security namespace

(defn verify-access! 
  "Verify access to a node with password, applying rate limiting logic"
  [node user password]
  (security/verify-access! node user password))

(defn unblock-node! 
  "Manually unblock a node"
  [node-id]
  (security/unblock-node! node-id))

(defn get-blocked-nodes 
  "Get all currently blocked nodes"
  []
  (security/get-blocked-nodes))

(defn save-state! 
  "Save security state to NBT compound tag"
  [nbt]
  (security/save-security-state! nbt))

(defn load-state! 
  "Load security state from NBT compound tag"
  [nbt]
  (security/load-security-state! nbt))

;; Deprecated functions - log a warning when used
;; These are maintained for backwards compatibility but will be removed in a future version

(defn- record-access-attempt! [node-id success?]
  (log/warn "Deprecated function called: record-access-attempt! - Use security/verify-access! instead")
  ;; Forward to the main implementation
  (let [now (System/currentTimeMillis)]
    (swap! security/security-state update-in [:access-rates node-id]
           #(conj (or % []) {:timestamp now :success success?}))))

(defn- check-rate-limit [node-id]
  (log/warn "Deprecated function called: check-rate-limit - Use security/verify-access! instead")
  (security/check-rate-limit node-id))