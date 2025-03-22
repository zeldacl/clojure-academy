(ns cn.academy.tech-system.energy-system.monitoring
  (:require [cn.academy.tech-system.energy-system.network.state :as network-state]
            [cn.academy.tech-system.energy-system.diagnostics :as diagnostics]
            [cn.academy.tech-system.energy-system.analytics :as analytics]
            [cn.academy.tech-system.energy-system.persistence :as persistence]
            [mcmod.nbt :as nbt]
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

;; Implement IPersistable to save/load monitoring state
(defrecord MonitoringSystem [state-atom]
  persistence/IPersistable
  
  (save-to-nbt! [_ tag]
    (let [state @state-atom
          error-tag (nbt/create-compound)
          health-tag (nbt/create-compound)]
      
      ;; Save error counts
      (doseq [[error-type count] (:error-counts state)]
        (nbt/put-int error-tag (name error-type) count))
      (nbt/put-tag tag "error_counts" error-tag)
      
      ;; Save health check summaries (just the status, not all issues)
      (doseq [[network-id health] (:health-checks state)]
        (let [net-tag (nbt/create-compound)]
          (nbt/put-long net-tag "timestamp" (:timestamp health))
          (nbt/put-string net-tag "status" (name (:status health)))
          (nbt/put-tag health-tag network-id net-tag)))
      (nbt/put-tag tag "health_checks" health-tag)
      
      ;; Save last check time
      (when-let [last-check (:last-check state)]
        (nbt/put-long tag "last_check" last-check))
      
      ;; Save check interval
      (nbt/put-int tag "check_interval" (:check-interval state))))
  
  (load-from-nbt! [_ tag]
    (let [error-counts (when-let [error-tag (nbt/get-compound tag "error_counts")]
                         (into {} (map (fn [key]
                                        [(keyword key) (nbt/get-int error-tag key)])
                                       (.get-keys error-tag))))
          
          health-checks (when-let [health-tag (nbt/get-compound tag "health_checks")]
                         (into {} (map (fn [network-id]
                                        (let [net-tag (nbt/get-compound health-tag network-id)]
                                          [network-id
                                           {:timestamp (nbt/get-long net-tag "timestamp")
                                            :status (keyword (nbt/get-string net-tag "status"))
                                            :issues []}]))
                                      (.get-keys health-tag))))
          
          last-check (when (nbt/contains? tag "last_check")
                       (nbt/get-long tag "last_check"))
          
          check-interval (nbt/get-int tag "check_interval" 60000)]
      
      (swap! state-atom (fn [state]
                         (cond-> state
                           error-counts (assoc :error-counts error-counts)
                           health-checks (assoc :health-checks health-checks)
                           last-check (assoc :last-check last-check)
                           check-interval (assoc :check-interval check-interval)))))))

;; Create monitoring system instance
(def monitoring-system (->MonitoringSystem monitor-state))

(defn start-monitoring! []
  ;; Register with persistence system
  (persistence/register-persistable! :monitoring monitoring-system)
  
  ;; Start monitoring thread
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