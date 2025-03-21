(ns cn.academy.tech-system.energy-system.diagnostics
  (:require [cn.academy.tech-system.energy-system.network.state :as network-state]
            [cn.academy.tech-system.energy-system.network.optimization :as optimization]
            [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [clojure.tools.logging :as log]))

(defn analyze-network [network-id]
  (when-let [network (network-state/get-network network-id)]
    (let [nodes (network-state/get-network-nodes network-id)
          node-states (for [node-id nodes
                           :when (wireless/is-wireless-node? node-id)]
                       {:id node-id
                        :energy (wireless/get-node-energy node-id)
                        :max-energy (wireless/get-node-max-energy node-id)
                        :bandwidth (wireless/get-node-bandwidth node-id)
                        :usage (optimization/get-bandwidth-usage node-id 60000)})]
      {:network-id network-id
       :node-count (count nodes)
       :total-energy (reduce + (map :energy node-states))
       :avg-energy (/ (reduce + (map :energy node-states))
                     (count node-states))
       :total-bandwidth (reduce + (map :bandwidth node-states))
       :bandwidth-usage (reduce + (map :usage node-states))
       :nodes node-states
       :issues (identify-issues network node-states)})))

(defn- identify-issues [network node-states]
  (vec
    (concat
      ;; Check for overloaded nodes
      (for [node node-states
            :let [energy-ratio (/ (:energy node) (:max-energy node))]
            :when (> energy-ratio 0.9)]
        {:type :node-overload
         :node-id (:id node)
         :severity :warning
         :message (format "Node %s is near capacity (%.1f%%)" 
                         (:id node) (* energy-ratio 100))})
      
      ;; Check for network congestion
      (when (> (:bandwidth-usage network) (* 0.8 (:total-bandwidth network)))
        [{:type :network-congestion
          :severity :warning
          :message "Network bandwidth usage is high"}])
      
      ;; Check for poorly distributed energy
      (let [energies (map :energy node-states)
            avg (/ (reduce + energies) (count energies))
            std-dev (Math/sqrt (/ (reduce + (map #(Math/pow (- % avg) 2) energies))
                                (count energies)))]
        (when (> (/ std-dev avg) 0.5)
          [{:type :unbalanced-energy
            :severity :info
            :message "Energy distribution is uneven"}])))))

(defn get-network-stats [network-id window]
  (when-let [network (network-state/get-network network-id)]
    (let [nodes (network-state/get-network-nodes network-id)
          stats (for [node-id nodes]
                 {:id node-id
                  :bandwidth-usage (optimization/get-bandwidth-usage node-id window)})]
      {:network-id network-id
       :timestamp (System/currentTimeMillis)
       :total-bandwidth-usage (reduce + (map :bandwidth-usage stats))
       :node-stats stats})))

(defn dump-network-state [network-id]
  (when-let [analysis (analyze-network network-id)]
    (log/info "Network State Dump:" network-id)
    (log/info "Total Nodes:" (:node-count analysis))
    (log/info "Total Energy:" (:total-energy analysis))
    (log/info "Average Energy:" (:avg-energy analysis))
    (log/info "Bandwidth Usage:" (:bandwidth-usage analysis))
    (log/info "Issues Found:" (count (:issues analysis)))
    (doseq [issue (:issues analysis)]
      (log/info (format "[%s] %s" (name (:severity issue)) (:message issue))))
    analysis))