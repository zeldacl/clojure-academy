(ns cn.academy.tech-system.energy-system.optimization
  (:require [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.monitoring :as monitoring]
            [cn.academy.tech-system.energy-system.debug :as debug]
            [clojure.tools.logging :as log]))

(def ^:private optimizer-state
  (atom {:optimization-enabled true
         :auto-balance-threshold 0.25  ; 25% energy difference triggers rebalancing
         :connection-optimization-interval 300000  ; 5 minutes
         :last-optimization {}}))

(defprotocol INetworkOptimizer
  (optimize-network! [this network])
  (optimize-connections! [this network])
  (balance-network-load! [this network])
  (suggest-network-improvements [this network]))

(defrecord NetworkOptimizer [state-atom]
  INetworkOptimizer
  (optimize-network! [this network]
    (when (:optimization-enabled @state-atom)
      (try
        (debug/log-network-change! network :optimization-started nil)
        (optimize-connections! this network)
        (balance-network-load! this network)
        (swap! state-atom assoc-in [:last-optimization (:id network)]
               {:timestamp (System/currentTimeMillis)
                :success true})
        (debug/log-network-change! network :optimization-completed nil)
        true
        (catch Exception e
          (log/error e "Failed to optimize network" (:id network))
          false))))
  
  (optimize-connections! [_ network]
    (let [nodes (network/get-nodes network)
          connections (for [node nodes
                          conn (network/get-connected-nodes node)]
                      {:source node
                       :target conn
                       :distance (network/calculate-distance node conn)
                       :efficiency (network/calculate-efficiency node conn)})]
      ;; Remove inefficient connections
      (doseq [{:keys [source target efficiency]} connections
              :when (< efficiency 0.5)]  ; Efficiency threshold
        (network/disconnect! source target)
        (debug/log-network-change! network :connection-removed
                                 {:source (wireless/get-id source)
                                  :target (wireless/get-id target)
                                  :reason :low-efficiency}))
      
      ;; Try to establish better connections
      (doseq [node nodes
              :let [current-connections (count (network/get-connected-nodes node))
                    capacity (wireless/get-capacity node)]
              :when (< current-connections capacity)]
        (when-let [best-candidate (->> nodes
                                     (remove #(network/connected? node %))
                                     (remove #{node})
                                     (filter #(network/in-range? node %))
                                     (sort-by #(network/calculate-efficiency node %))
                                     last)]
          (network/connect! node best-candidate)
          (debug/log-network-change! network :connection-added
                                   {:source (wireless/get-id node)
                                    :target (wireless/get-id best-candidate)})))))
  
  (balance-network-load! [_ network]
    (let [nodes (network/get-nodes network)
          threshold (:auto-balance-threshold @state-atom)]
      (loop [iterations 0]
        (let [energies (map wireless/get-energy nodes)
              avg-energy (/ (reduce + energies) (count nodes))
              max-diff (apply max (map #(Math/abs (- % avg-energy)) energies))]
          (when (and (< iterations 10)  ; Prevent infinite loops
                     (> (/ max-diff avg-energy) threshold))
            ;; Transfer energy from high to low nodes
            (doseq [node nodes
                    :let [energy (wireless/get-energy node)]
                    :when (> energy avg-energy)]
              (let [excess (- energy avg-energy)
                    targets (filter #(< (wireless/get-energy %) avg-energy) nodes)]
                (when (seq targets)
                  (let [per-target (/ excess (count targets))]
                    (doseq [target targets]
                      (network/transfer-energy! node target per-target))))))
            (recur (inc iterations)))))))
  
  (suggest-network-improvements [_ network]
    (let [nodes (network/get-nodes network)
          metrics (monitoring/collect-metrics! monitoring/monitor network)
          health (monitoring/get-network-health monitoring/monitor network)]
      {:suggestions
       (concat
         ;; Suggest adding nodes for better coverage
         (when (< (count nodes) 3)
           [{:type :add-nodes
             :priority :high
             :reason "Network requires more nodes for reliability"}])
         
         ;; Suggest upgrading nodes with consistently high load
         (for [node nodes
               :let [energy-ratio (/ (wireless/get-energy node)
                                   (wireless/get-max-energy node))]
               :when (> energy-ratio 0.9)]
           {:type :upgrade-node
            :node-id (wireless/get-id node)
            :priority :medium
            :reason "Node frequently at capacity"})
         
         ;; Suggest topology improvements
         (when (> (:connection-count metrics) (* 2 (count nodes)))
           [{:type :optimize-topology
             :priority :low
             :reason "Network has excess connections"}]))})))

(def optimizer (->NetworkOptimizer optimizer-state))

(defn start-optimization-cycle! []
  (let [optimization-thread
        (Thread.
          (fn []
            (try
              (while true
                (when (:optimization-enabled @optimizer-state)
                  (doseq [[id network] (network/get-all-networks)]
                    (try
                      (optimize-network! optimizer network)
                      (catch Exception e
                        (log/error e "Error during network optimization" id)))))
                (Thread/sleep (:connection-optimization-interval @optimizer-state)))
              (catch InterruptedException _))))]
    (.setDaemon optimization-thread true)
    (.start optimization-thread)
    optimization-thread))