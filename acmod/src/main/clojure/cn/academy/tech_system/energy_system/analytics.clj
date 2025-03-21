(ns cn.academy.tech-system.energy-system.analytics
  (:require [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.monitoring :as monitoring]
            [clojure.tools.logging :as log]))

(def ^:private analytics-state
  (atom {:stats {}
         :hourly-snapshots []
         :daily-summaries []
         :retention-days 7}))

(defprotocol INetworkAnalytics
  (collect-stats! [this network timestamp])
  (analyze-trends [this network-id timespan])
  (generate-report [this network-id])
  (predict-usage [this network-id hours]))

(defrecord NetworkAnalytics [state-atom]
  INetworkAnalytics
  (collect-stats! [_ network timestamp]
    (let [nodes (network/get-nodes network)
          stats {:timestamp timestamp
                 :node-count (count nodes)
                 :total-energy (reduce + (map wireless/get-energy nodes))
                 :avg-energy (if (seq nodes)
                             (/ (reduce + (map wireless/get-energy nodes))
                                (count nodes))
                             0)
                 :connections (reduce + (map #(count (network/get-connected-nodes %))
                                           nodes))
                 :transfer-rate (network/get-transfer-rate network)}]
      (swap! state-atom update-in [:stats (:id network)]
             (fnil conj []) stats)
      ;; Maintain history size
      (when (> (count (get-in @state-atom [:stats (:id network)]))
               (* 24 (:retention-days @state-atom)))
        (swap! state-atom update-in [:stats (:id network)] 
               #(vec (take-last (* 24 (:retention-days @state-atom)) %))))
      stats))
  
  (analyze-trends [_ network-id timespan]
    (when-let [stats (get-in @state-atom [:stats network-id])]
      (let [recent-stats (take-last timespan stats)
            periods (partition 2 1 recent-stats)]
        {:energy-trend
         (let [changes (for [[prev curr] periods]
                        (- (:total-energy curr)
                           (:total-energy prev)))]
           {:direction (if (pos? (reduce + changes)) :increasing :decreasing)
            :rate (if (seq changes)
                   (/ (reduce + changes) (count changes))
                   0)})
         
         :stability
         (let [energy-stddev (when (seq recent-stats)
                              (let [energies (map :total-energy recent-stats)
                                    mean (/ (reduce + energies) (count energies))]
                                (Math/sqrt (/ (reduce + (map #(Math/pow (- % mean) 2)
                                                           energies))
                                            (count energies)))))]
           {:score (if energy-stddev
                    (- 1.0 (min 1.0 (/ energy-stddev
                                      (:avg-energy (last recent-stats)))))
                    1.0)
            :fluctuations (count (filter #(> (Math/abs %) 1000)
                                       (map - (map :total-energy (rest recent-stats))
                                             (map :total-energy (butlast recent-stats)))))})
         
         :efficiency
         (let [transfer-rates (map :transfer-rate recent-stats)]
           {:average (if (seq transfer-rates)
                      (/ (reduce + transfer-rates) (count transfer-rates))
                      0)
            :peak (apply max 0 transfer-rates)})})))
  
  (generate-report [this network-id]
    (when-let [stats (get-in @state-atom [:stats network-id])]
      (let [current (last stats)
            trends (analyze-trends this network-id 24)
            daily-avg (/ (reduce + (map :total-energy (take-last 24 stats)))
                        24)]
        {:timestamp (:timestamp current)
         :current-state
         {:node-count (:node-count current)
          :total-energy (:total-energy current)
          :connections (:connections current)}
         :trends trends
         :daily-statistics
         {:average-energy daily-avg
          :peak-energy (apply max (map :total-energy (take-last 24 stats)))
          :min-energy (apply min (map :total-energy (take-last 24 stats)))}
         :recommendations
         (cond-> []
           (< (:score (:stability trends)) 0.7)
           (conj {:type :improve-stability
                  :priority :high
                  :reason "Network showing significant energy fluctuations"})
           
           (< (:average (:efficiency trends)) 500)
           (conj {:type :improve-efficiency
                  :priority :medium
                  :reason "Network transfer rates below optimal levels"}))})))
  
  (predict-usage [_ network-id hours]
    (when-let [stats (get-in @state-atom [:stats network-id])]
      (let [recent-stats (take-last (* 24 7) stats) ; Use last week's data
            hourly-patterns (reduce (fn [acc stat]
                                    (let [hour (mod (quot (:timestamp stat) 3600000) 24)]
                                      (update acc hour
                                              #(conj (or % []) (:total-energy stat)))))
                                  {}
                                  recent-stats)
            predictions
            (for [hour (range hours)
                  :let [target-hour (mod hour 24)
                        historical (get hourly-patterns target-hour [])]]
              {:hour hour
               :predicted-energy (if (seq historical)
                                 (/ (reduce + historical) (count historical))
                                 0)
               :confidence (min 1.0 (/ (count historical) 7))}))]
        {:predictions predictions
         :reliability (/ (count (filter #(>= (:confidence %) 0.5) predictions))
                        (count predictions))}))))

(def analytics (->NetworkAnalytics analytics-state))

(defn start-analytics-collection! []
  (let [collection-thread
        (Thread.
          (fn []
            (try
              (while true
                (let [timestamp (System/currentTimeMillis)]
                  (doseq [[id network] (network/get-all-networks)]
                    (try
                      (collect-stats! analytics network timestamp)
                      (catch Exception e
                        (log/error e "Error collecting network statistics" id)))))
                (Thread/sleep 3600000)) ; Collect hourly stats
              (catch InterruptedException _))))]
    (.setDaemon collection-thread true)
    (.start collection-thread)
    collection-thread))