(ns cn.academy.tech-system.energy-system.debug
  (:require [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.error-handling :as error]
            [clojure.tools.logging :as log]
            [clojure.string :as str]))

(def debug-state
  (atom {:enabled false
         :trace-energy-transfers true
         :trace-network-changes true
         :trace-security-events true
         :history []}))

(defn enable-debug! []
  (swap! debug-state assoc :enabled true))

(defn disable-debug! []
  (swap! debug-state assoc :enabled false))

(defn clear-history! []
  (swap! debug-state assoc :history []))

(defn- add-debug-entry! [type data]
  (when (:enabled @debug-state)
    (swap! debug-state update :history conj
           (assoc data
                  :type type
                  :timestamp (System/currentTimeMillis)))))

(defn log-energy-transfer! [source target amount success?]
  (when (and (:enabled @debug-state) 
             (:trace-energy-transfers @debug-state))
    (add-debug-entry! :energy-transfer
                     {:source-id (wireless/get-id source)
                      :target-id (wireless/get-id target)
                      :amount amount
                      :success success?})
    (log/debug (str "Energy transfer: " amount " FE from "
                   (wireless/get-id source) " to "
                   (wireless/get-id target)
                   (when-not success? " (failed)")))))

(defn log-network-change! [network event data]
  (when (and (:enabled @debug-state)
             (:trace-network-changes @debug-state))
    (add-debug-entry! :network-change
                     (assoc data
                            :network-id (:id network)
                            :event event))
    (log/debug (str "Network " (:id network) " " 
                   (name event) ": "
                   (pr-str data)))))

(defn log-security-event! [node player event result]
  (when (and (:enabled @debug-state)
             (:trace-security-events @debug-state))
    (add-debug-entry! :security-event
                     {:node-id (wireless/get-id node)
                      :player-id (.getUniqueID player)
                      :event event
                      :result result})
    (log/debug (str "Security event: " (name event)
                   " by player " (.getUniqueID player)
                   " on node " (wireless/get-id node)
                   " - " (if result "allowed" "denied")))))

(defn get-debug-history 
  ([] (:history @debug-state))
  ([type] (filter #(= type (:type %)) (:history @debug-state))))

(defn dump-network-state! [network]
  (when (:enabled @debug-state)
    (let [nodes (network/get-nodes network)
          total-energy (reduce + (map wireless/get-energy nodes))
          output [(str "Network State Dump - " (:id network))
                 (str "Total Nodes: " (count nodes))
                 (str "Total Energy: " total-energy)
                 ""
                 "Node Details:"]]
      (doseq [node nodes]
        (let [pos (wireless/get-position node)
              connections (count (network/get-connected-nodes node))]
          (conj! output
                 (str/join " "
                          [(wireless/get-id node)
                           (str "(" (:x pos) "," (:y pos) "," (:z pos) ")")
                           "Energy:" (wireless/get-energy node)
                           "Connections:" connections]))))
      (log/info (str/join "\n" output))))))