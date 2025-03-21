(ns cn.academy.tech-system.energy-system.monitoring
  (:require [cn.academy.tech-system.energy-system.network.state :as network-state]
            [cn.academy.tech-system.energy-system.diagnostics :as diagnostics]
            [cn.academy.tech-system.energy-system.analytics :as analytics]
            [clojure.tools.logging :as log]))

(def ^:private monitor-state
  (atom {:error-counts {}
         :health-checks {}
         :last-check nil
         :check-interval 60000}))

(defn- record-error! [error-type error-data]
  (let [now (System/currentTimeMillis)]
    (swap! monitor-state update-in [:error-counts error-type] 
           (fnil inc 0))
    (log/error "Energy system error:" error-type error-data)))

(defn- check-network-health! [network-id]
  (try 
    (when-let [analysis (diagnostics/analyze-network network-id)]
      (let [issues (:issues analysis)
            critical-issues (filter #(= :critical (:severity %)) issues)]
        (swap! monitor-state assoc-in 
               [:health-checks network-id]
               {:timestamp (System/currentTimeMillis)
                :status (if (seq critical-issues) :unhealthy :healthy)
                :issues issues})
        (when (seq critical-issues)
          (record-error! :network-health 
                        {:network-id network-id 
                         :issues critical-issues}))))
    (catch Exception e
      (record-error! :health-check-failed 
                     {:network-id network-id
                      :error (.getMessage e)}))))

(defn- monitor-tick! []
  (let [now (System/currentTimeMillis)
        last-check (:last-check @monitor-state)
        interval (:check-interval @monitor-state)]
    (when (or (nil? last-check)
              (> (- now last-check) interval))
      (doseq [network-id (keys (network-state/get-all-networks))]
        (check-network-health! network-id))
      (swap! monitor-state assoc :last-check now))))

(defn get-network-health [network-id]
  (get-in @monitor-state [:health-checks network-id]))

(defn get-error-stats []
  (:error-counts @monitor-state))

(defn generate-health-report []
  (let [networks (network-state/get-all-networks)
        health-checks (vals (:health-checks @monitor-state))
        unhealthy-count (count (filter #(= :unhealthy (:status %)) health-checks))]
    {:timestamp (System/currentTimeMillis)
     :total-networks (count networks)
     :unhealthy-networks unhealthy-count
     :error-counts (get-error-stats)
     :health-status (if (zero? unhealthy-count) :healthy :degraded)}))

(defn start-monitoring! []
  (let [monitor-thread
        (Thread.
          (fn []
            (try
              (while true
                (monitor-tick!)
                (Thread/sleep (:check-interval @monitor-state)))
              (catch InterruptedException _))))]
    (.setDaemon monitor-thread true)
    (.start monitor-thread)
    (log/info "Energy system monitoring started")))