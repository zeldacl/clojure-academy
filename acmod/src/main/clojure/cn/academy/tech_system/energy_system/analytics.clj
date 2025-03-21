(ns cn.academy.tech-system.energy-system.analytics
  (:require [cn.academy.tech-system.energy-system.network.optimization :as optimization]
            [cn.academy.tech-system.energy-system.network.state :as network-state]
            [cn.academy.tech-system.energy-system.config :as config]
            [clojure.tools.logging :as log]))

(def ^:private analytics-state
  (atom {:metrics {}
         :samples {}
         :window-size 300000})) ; 5 minute window

(defn- record-metric! [metric-type node-id value]
  (let [now (System/currentTimeMillis)
        window-size (config/get-config [:analytics :window-size] 300000)]
    (swap! analytics-state update-in [:metrics metric-type node-id]
           (fn [samples]
             (->> (or samples [])
                  (filter #(> (:timestamp %) (- now window-size)))
                  (conj {:timestamp now :value value}))))))

(defn get-metric-stats [metric-type node-id]
  (let [samples (get-in @analytics-state [:metrics metric-type node-id])
        values (map :value samples)]
    (when (seq values)
      {:min (apply min values)
       :max (apply max values)
       :avg (/ (reduce + values) (count values))
       :count (count values)})))

(defn track-network-metrics! [network-id]
  (when-let [network (network-state/get-network network-id)]
    (let [nodes (network-state/get-network-nodes network-id)]
      (doseq [node-id nodes]
        (let [bandwidth-usage (optimization/get-bandwidth-usage node-id 60000)]
          (record-metric! :bandwidth node-id bandwidth-usage))))))

(defn analyze-network-performance [network-id]
  (when-let [network (network-state/get-network network-id)]
    (let [nodes (network-state/get-network-nodes network-id)
          node-metrics (for [node-id nodes]
                        {:id node-id
                         :bandwidth (get-metric-stats :bandwidth node-id)})]
      {:network-id network-id
       :timestamp (System/currentTimeMillis)
       :nodes node-metrics
       :total-bandwidth (reduce + (map #(get-in % [:bandwidth :avg] 0) node-metrics))
       :active-nodes (count (filter #(pos? (get-in % [:bandwidth :avg] 0)) node-metrics))})))

(defn generate-performance-report [network-id]
  (when-let [analysis (analyze-network-performance network-id)]
    (let [{:keys [total-bandwidth active-nodes nodes]} analysis
          high-usage-nodes (filter #(> (get-in % [:bandwidth :avg] 0) 
                                     (config/get-config [:network :bandwidth-warning-threshold] 5000))
                                  nodes)]
      (when (seq high-usage-nodes)
        (log/warn "High bandwidth usage detected in network" network-id 
                 "- Nodes:" (map :id high-usage-nodes)))
      (assoc analysis
             :performance-rating 
             (cond 
               (> total-bandwidth (* active-nodes 10000)) :poor
               (> total-bandwidth (* active-nodes 5000)) :fair
               :else :good)))))

(defn init! []
  (let [metric-interval (config/get-config [:analytics :metric-interval] 60000)]
    (mcmod.scheduler/schedule-recurring 
      metric-interval
      #(doseq [network-id (keys (network-state/get-all-networks))]
         (track-network-metrics! network-id)))
    (log/info "Network analytics system initialized")))