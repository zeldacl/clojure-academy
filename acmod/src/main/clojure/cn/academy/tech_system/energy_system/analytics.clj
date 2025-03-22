(ns cn.academy.tech-system.energy-system.analytics
  (:require [cn.academy.tech-system.energy-system.network.state :as network-state]
            [cn.academy.tech-system.energy-system.persistence :as persistence]
            [mcmod.nbt :as nbt]
            [clojure.tools.logging :as log]))

;; Analytics system for tracking network usage patterns and making predictions
(def ^:private analytics-state
  (atom {:stats {}  ;; Map of network-id -> collection of historical stats
         :sampling-interval 300000  ;; 5 minutes in ms
         :max-samples 288  ;; 24 hours of samples at 5-minute intervals
         :last-sample nil}))

(defn- collect-network-stats! [network-id]
  (when-let [network (network-state/get-network network-id)]
    (let [nodes (network-state/get-network-nodes network-id)
          now (System/currentTimeMillis)
          node-count (count nodes)
          total-energy (reduce + (map :energy nodes))
          connections (count (filter :connected nodes))
          transfer-rate (reduce + (map #(get % :last-transfer-rate 0) nodes))]
      {:timestamp now
       :node-count node-count
       :total-energy total-energy
       :connections connections
       :transfer-rate transfer-rate})))

(defn- collect-all-stats! []
  (let [now (System/currentTimeMillis)
        networks (network-state/get-all-networks)]
    (doseq [[network-id _] networks]
      (when-let [stats (collect-network-stats! network-id)]
        (swap! analytics-state update-in [:stats network-id]
               (fn [samples]
                 (let [new-samples (conj (or samples []) stats)
                       max-samples (:max-samples @analytics-state)]
                   (if (> (count new-samples) max-samples)
                     (subvec new-samples 
                             (- (count new-samples) max-samples))
                     new-samples))))))
    (swap! analytics-state assoc :last-sample now)))

(defn generate-report [analytics network-id]
  (when-let [stats (get-in @(:state-atom analytics) [:stats network-id])]
    (let [current (last stats)
          daily-stats (take-last 288 stats)  ;; Last 24 hours (at 5-min intervals)
          energy-values (map :total-energy daily-stats)
          avg-energy (when (seq energy-values)
                      (/ (reduce + energy-values) (count energy-values)))
          peak-energy (when (seq energy-values)
                       (apply max energy-values))
          min-energy (when (seq energy-values)
                      (apply min energy-values))
          
          ;; Simple trend analysis
          energy-diff (when (>= (count stats) 2)
                        (- (:total-energy (last stats))
                           (:total-energy (first stats))))
          energy-trend (cond
                        (nil? energy-diff) :stable
                        (> energy-diff 0) :increasing
                        (< energy-diff 0) :decreasing
                        :else :stable)
          
          ;; Generate recommendations
          recommendations (filter identity
                                 [(when (and peak-energy (> peak-energy 0))
                                    (if (> (/ peak-energy (or min-energy 1)) 5)
                                     {:type :balance
                                      :priority :high
                                      :reason "High energy usage volatility detected"}
                                     nil))
                                 
                                  (when (and avg-energy current)
                                    (if (> (:total-energy current) (* avg-energy 1.5))
                                     {:type :expansion
                                      :priority :medium
                                      :reason "Current energy usage significantly above average"}
                                     nil))
                                  
                                  (when (and (= energy-trend :increasing) current)
                                    {:type :capacity
                                     :priority :medium
                                     :reason "Energy usage trending upward, consider capacity planning"})])]
      
      {:current-state current
       :daily-statistics {:average-energy avg-energy
                          :peak-energy peak-energy
                          :min-energy min-energy}
       :trends {:energy-trend {:direction energy-trend
                               :rate energy-diff}}
       :recommendations recommendations})))

(defn analyze-trends [analytics network-id hours]
  (when-let [stats (get-in @(:state-atom analytics) [:stats network-id])]
    (let [hours-in-samples (int (/ (* hours 60) 5))  ;; Convert hours to 5-min samples
          recent-stats (take-last hours-in-samples stats)]
      (when (>= (count recent-stats) 2)
        (let [first-energy (:total-energy (first recent-stats))
              last-energy (:total-energy (last recent-stats))
              energy-diff (- last-energy first-energy)
              energy-rate (/ energy-diff (count recent-stats))
              
              ;; Calculate efficiency
              transfer-rates (map :transfer-rate recent-stats)
              energy-changes (map #(Math/abs (- (:total-energy %1) (:total-energy %2)))
                                 (rest recent-stats) 
                                 (butlast recent-stats))
              efficiencies (map #(if (zero? %1) 1.0 (min 1.0 (/ %2 (max 1 %1))))
                               transfer-rates
                               energy-changes)
              avg-efficiency (when (seq efficiencies)
                              (/ (reduce + efficiencies) (count efficiencies)))
              peak-efficiency (when (seq efficiencies)
                               (apply max efficiencies))
              
              ;; Calculate stability
              energy-diffs (map #(Math/abs (- (:total-energy %1) (:total-energy %2)))
                              (rest recent-stats)
                              (butlast recent-stats))
              avg-diff (when (seq energy-diffs)
                        (/ (reduce + energy-diffs) (count energy-diffs)))
              stability-score (if (and avg-diff (pos? avg-diff) (pos? last-energy))
                               (- 1.0 (min 1.0 (/ avg-diff last-energy)))
                               1.0)]
          
          {:energy-trend {:direction (cond
                                     (> energy-diff 0) :increasing
                                     (< energy-diff 0) :decreasing
                                     :else :stable)
                         :rate energy-rate}
           :efficiency {:average avg-efficiency
                        :peak peak-efficiency}
           :stability {:score stability-score}}))))

(defn predict-usage [analytics network-id hours]
  (when-let [stats (get-in @(:state-atom analytics) [:stats network-id])]
    (when (>= (count stats) 24)  ;; Need at least 2 hours of data for prediction
      (let [;; Simple linear regression
            x-values (range (count stats))
            y-values (map :total-energy stats)
            x-mean (/ (reduce + x-values) (count x-values))
            y-mean (/ (reduce + y-values) (count y-values))
            
            ;; Calculate slope and intercept
            numerator (reduce + (map #(* (- %1 x-mean) (- %2 y-mean))
                                    x-values y-values))
            denominator (reduce + (map #(Math/pow (- % x-mean) 2) x-values))
            slope (if (zero? denominator) 0 (/ numerator denominator))
            intercept (- y-mean (* slope x-mean))
            
            ;; Function to predict energy at time t
            predict-fn (fn [t] (+ intercept (* slope t)))
            
            ;; Calculate R-squared (coefficient of determination)
            ss-total (reduce + (map #(Math/pow (- % y-mean) 2) y-values))
            ss-residual (reduce + (map #(Math/pow (- %1 (predict-fn %2)) 2)
                                      y-values x-values))
            r-squared (if (zero? ss-total) 0 (- 1.0 (/ ss-residual ss-total)))
            reliability (max 0.0 (min 1.0 r-squared))
            
            ;; Generate predictions for requested hours
            predictions (for [hour (range (inc hours))]
                         (let [sample-offset (* hour 12)  ;; 12 samples per hour at 5-min intervals
                               time-point (+ (count stats) sample-offset)
                               predicted (predict-fn time-point)
                               ;; Confidence decreases with prediction distance
                               confidence (max 0.0 (* reliability (- 1.0 (/ hour hours))))]
                           {:hour hour
                            :predicted-energy (max 0 predicted)
                            :confidence confidence}))]
        
        {:reliability reliability
         :predictions predictions}))))

;; Implement IPersistable for analytics persistence
(defrecord AnalyticsSystem [state-atom]
  persistence/IPersistable
  
  (save-to-nbt! [_ tag]
    (let [state @state-atom
          stats-tag (nbt/create-compound)]
      
      ;; Save sampling configuration
      (nbt/put-int tag "sampling_interval" (:sampling-interval state))
      (nbt/put-int tag "max_samples" (:max-samples state))
      
      ;; Save last sample timestamp
      (when-let [last-sample (:last-sample state)]
        (nbt/put-long tag "last_sample" last-sample))
      
      ;; Save network stats
      (doseq [[network-id network-stats] (:stats state)]
        (when (seq network-stats)
          (let [net-tag (nbt/create-compound)
                timestamps (long-array (map :timestamp network-stats))
                energies (long-array (map :total-energy network-stats))
                nodes (int-array (map :node-count network-stats))
                connections (int-array (map :connections network-stats))
                transfer-rates (long-array (map :transfer-rate network-stats))]
            
            ;; Save arrays of data
            (nbt/put-long-array net-tag "timestamps" timestamps)
            (nbt/put-long-array net-tag "energies" energies)
            (nbt/put-int-array net-tag "nodes" nodes)
            (nbt/put-int-array net-tag "connections" connections)
            (nbt/put-long-array net-tag "transfer_rates" transfer-rates)
            
            (nbt/put-tag stats-tag network-id net-tag))))
      
      (nbt/put-tag tag "stats" stats-tag)))
  
  (load-from-nbt! [_ tag]
    (let [sampling-interval (nbt/get-int tag "sampling_interval" 300000)
          max-samples (nbt/get-int tag "max_samples" 288)
          last-sample (when (nbt/contains? tag "last_sample")
                        (nbt/get-long tag "last_sample"))
          
          stats (when-let [stats-tag (nbt/get-compound tag "stats")]
                  (into {} 
                        (map 
                         (fn [network-id]
                           (let [net-tag (nbt/get-compound stats-tag network-id)
                                 timestamps (nbt/get-long-array net-tag "timestamps")
                                 energies (nbt/get-long-array net-tag "energies")
                                 nodes (nbt/get-int-array net-tag "nodes")
                                 connections (nbt/get-int-array net-tag "connections")
                                 transfer-rates (nbt/get-long-array net-tag "transfer_rates")
                                 count (alength timestamps)
                                 
                                 ;; Reconstruct stats objects from arrays
                                 stats (mapv 
                                        (fn [i]
                                          {:timestamp (aget timestamps i)
                                           :total-energy (aget energies i)
                                           :node-count (aget nodes i)
                                           :connections (aget connections i)
                                           :transfer-rate (aget transfer-rates i)})
                                        (range count))]
                             [network-id stats]))
                         (.get-keys stats-tag))))]
      
      ;; Update state atomically
      (swap! state-atom (fn [state]
                         (cond-> state
                           true (assoc :sampling-interval sampling-interval
                                     :max-samples max-samples)
                           last-sample (assoc :last-sample last-sample)
                           stats (assoc :stats stats)))))))

;; Create analytics system instance
(def analytics (->AnalyticsSystem analytics-state))

(defn init! []
  ;; Register with persistence system
  (persistence/register-persistable! :analytics analytics)
  
  ;; Start sampling thread
  (let [sampling-thread
        (Thread.
         (fn []
           (try
             (while true
               (let [{:keys [sampling-interval last-sample]} @analytics-state
                     now (System/currentTimeMillis)]
                 (when (or (nil? last-sample)
                          (>= (- now last-sample) sampling-interval))
                   (collect-all-stats!))
                 (Thread/sleep 10000))  ;; Check every 10 seconds
             (catch InterruptedException _)))))]
    
    (.setDaemon sampling-thread true)
    (.start sampling-thread)
    (log/info "Energy analytics system initialized with sampling interval of" 
             (int (/ (:sampling-interval @analytics-state) 1000)) "seconds")))