(ns cn.academy.tech-system.energy-system.monitoring
  (:require [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.error-handling :as error]
            [clojure.tools.logging :as log]))

(def ^:private monitor-state
  (atom {:metrics {}
         :alerts []
         :thresholds {:energy-low 0.1  ; 10% capacity
                     :energy-critical 0.05  ; 5% capacity
                     :connection-high 0.9  ; 90% of max connections
                     :latency-high 100}})) ; 100ms

(defprotocol INetworkMonitor
  (collect-metrics! [this network])
  (check-alerts! [this network])
  (get-network-health [this network])
  (get-metrics-history [this network-id metric-type]))

(defrecord NetworkMetrics [timestamp node-count total-energy avg-energy transfer-rate connection-count])

(defrecord NetworkMonitor [state-atom]
  INetworkMonitor
  (collect-metrics! [_ network]
    (let [nodes (network/get-nodes network)
          timestamp (System/currentTimeMillis)
          metrics (->NetworkMetrics
                   timestamp
                   (count nodes)
                   (reduce + (map wireless/get-energy nodes))
                   (if (seq nodes)
                     (/ (reduce + (map wireless/get-energy nodes)) (count nodes))
                     0)
                   (network/get-transfer-rate network)
                   (reduce + (map #(count (network/get-connected-nodes %)) nodes)))]
      (swap! state-atom update-in [:metrics (:id network)]
             (fnil conj []) metrics)
      metrics))
  
  (check-alerts! [_ network]
    (let [nodes (network/get-nodes network)
          metrics (collect-metrics! network)
          thresholds (:thresholds @state-atom)]
      (when (seq nodes)
        (let [alerts
              (concat
                ;; Check energy levels
                (for [node nodes
                      :let [energy-ratio (/ (wireless/get-energy node)
                                          (wireless/get-max-energy node))]
                      :when (< energy-ratio (:energy-critical thresholds))]
                  {:type :energy-critical
                   :node-id (wireless/get-id node)
                   :value energy-ratio})
                
                ;; Check connection counts
                (for [node nodes
                      :let [conn-ratio (/ (count (network/get-connected-nodes node))
                                        (wireless/get-capacity node))]
                      :when (> conn-ratio (:connection-high thresholds))]
                  {:type :connection-overload
                   :node-id (wireless/get-id node)
                   :value conn-ratio}))]
          
          (when (seq alerts)
            (swap! state-atom update :alerts concat alerts)
            (log/warn "Network alerts for" (:id network) ":" (pr-str alerts)))))))
  
  (get-network-health [_ network]
    (let [nodes (network/get-nodes network)
          metrics (collect-metrics! network)]
      {:status (cond
                 (empty? nodes) :offline
                 (some #(< (/ (wireless/get-energy %)
                             (wireless/get-max-energy %))
                          (:energy-critical @state-atom))
                      nodes) :critical
                 (some #(< (/ (wireless/get-energy %)
                             (wireless/get-max-energy %))
                          (:energy-low @state-atom))
                      nodes) :warning
                 :else :healthy)
       :metrics metrics
       :alerts (take-last 5 (:alerts @state-atom))}))
  
  (get-metrics-history [_ network-id metric-type]
    (when-let [metrics (get-in @state-atom [:metrics network-id])]
      (case metric-type
        :energy (map #(select-keys % [:timestamp :total-energy :avg-energy]) metrics)
        :connections (map #(select-keys % [:timestamp :connection-count]) metrics)
        :transfer-rate (map #(select-keys % [:timestamp :transfer-rate]) metrics)
        metrics))))

(def monitor (->NetworkMonitor monitor-state))

(defn start-monitoring! []
  (let [monitoring-thread
        (Thread.
          (fn []
            (try
              (while true
                (doseq [[id network] (network/get-all-networks)]
                  (try
                    (collect-metrics! monitor network)
                    (check-alerts! monitor network)
                    (catch Exception e
                      (error/handle-error! error/error-handler e
                        {:component :monitoring
                         :network-id id}))))
                (Thread/sleep 5000))
              (catch InterruptedException _))))]
    (.setDaemon monitoring-thread true)
    (.start monitoring-thread)
    monitoring-thread))