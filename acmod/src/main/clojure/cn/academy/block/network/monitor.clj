(ns cn.academy.block.network.monitor
  (:require [cn.academy.core.monitoring :as monitoring]
            [cn.academy.core.profiling :as profiling]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Network state tracking
(def network-state
  (atom {:active-networks {}
         :message-counts {}}))

;; Network performance tracking
(defrecord NetworkMonitor [network-id]
  monitoring/IMetricsCollector
  (record-metric! [_ name value]
    (monitoring/with-metrics "network" name value))
  
  (get-metric [_ name]
    (monitoring/get-metric "network" name)))

;; Message tracking
(defn track-message!
  "Track network message"
  [network-id message-type size]
  (profiling/with-profile "network" "message"
    (let [monitor (NetworkMonitor. network-id)]
      (.record-metric! monitor "message-size" size)
      (.record-metric! monitor "message-count" 1)
      (swap! network-state update-in 
             [:message-counts network-id message-type] 
             (fnil inc 0)))))

;; Network registration
(defn register-network!
  "Register network for monitoring"
  [network-id config]
  (swap! network-state assoc-in [:active-networks network-id] config))

(defn unregister-network!
  "Unregister network from monitoring"
  [network-id]
  (swap! network-state update :active-networks dissoc network-id))

;; Network diagnostics
(defn check-network-health
  "Check health of network"
  [network-id]
  (let [config (get-in @network-state [:active-networks network-id])
        message-counts (get-in @network-state [:message-counts network-id])
        monitor (NetworkMonitor. network-id)]
    {:id network-id
     :config config
     :messages message-counts
     :metrics {:message-rate (.get-metric monitor "message-count")
              :avg-size (.get-metric monitor "message-size")}}))

(defn generate-network-report
  "Generate network diagnostic report"
  []
  (let [networks (:active-networks @network-state)]
    (into {}
          (for [[id _] networks]
            [id (check-network-health id)]))))

;; Network error detection
(defn detect-network-issues!
  "Check for network issues"
  []
  (doseq [[id health] (generate-network-report)]
    (let [msg-rate (get-in health [:metrics :message-rate])]
      (when (and msg-rate (> msg-rate 1000))
        (error/set-error! id
          {:type :network
           :message (format "High message rate detected: %d/s" msg-rate)})))))

;; Initialize monitoring
(defn init-network-monitor! []
  (reset! network-state {:active-networks {}
                        :message-counts {}})
  ;; Start periodic health checks
  (future
    (try
      (while true
        (Thread/sleep 5000)
        (detect-network-issues!))
      (catch InterruptedException _))))