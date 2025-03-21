(ns cn.academy.tech-system.energy-system.testing
  (:require [cn.academy.tech-system.energy-system.network.state :as network-state]
            [cn.academy.tech-system.energy-system.network.optimization :as optimization]
            [cn.academy.tech-system.energy-system.transfer :as transfer]
            [cn.academy.tech-system.energy-system.diagnostics :as diagnostics]
            [clojure.tools.logging :as log]))

(defn create-test-network! [node-count]
  "Create a test network with specified number of nodes"
  (let [network-id (str (random-uuid))
        nodes (for [i (range node-count)]
                {:id (str "test-node-" i)
                 :energy 0
                 :max-energy 10000
                 :bandwidth 1000})]
    (network-state/register-network!
      network-id
      {:id network-id
       :nodes (into #{} (map :id nodes))
       :properties {:test true}})
    (doseq [node nodes]
      (network-state/add-node! network-id (:id node)))
    network-id))

(defn simulate-energy-transfer! [network-id cycles]
  "Simulate energy transfer between nodes for testing"
  (when-let [network (network-state/get-network network-id)]
    (let [nodes (network-state/get-network-nodes network-id)]
      (dotimes [_ cycles]
        (let [source (rand-nth (vec nodes))
              target (rand-nth (vec (disj (set nodes) source)))
              amount (rand-int 1000)]
          (transfer/transfer-energy! source target amount))))
    (diagnostics/dump-network-state network-id)))

(defn test-network-stability! [network-id duration]
  "Test network stability over time"
  (let [start-time (System/currentTimeMillis)
        end-time (+ start-time duration)]
    (loop [last-check start-time
           issues []]
      (if (< (System/currentTimeMillis) end-time)
        (let [current-time (System/currentTimeMillis)
              analysis (diagnostics/analyze-network network-id)
              new-issues (:issues analysis)]
          (when (seq new-issues)
            (log/warn "Found issues during stability test:" new-issues))
          (Thread/sleep 1000)
          (recur current-time (into issues new-issues)))
        {:total-issues (count issues)
         :issues-by-type (frequencies (map :type issues))
         :test-duration (- (System/currentTimeMillis) start-time)}))))

(defn benchmark-network! [network-id operations]
  "Benchmark network performance"
  (let [start-time (System/currentTimeMillis)]
    (dotimes [_ operations]
      (simulate-energy-transfer! network-id 1))
    (let [end-time (System/currentTimeMillis)
          duration (- end-time start-time)]
      {:operations operations
       :duration duration
       :ops-per-second (double (/ operations (/ duration 1000)))
       :bandwidth-usage (optimization/get-bandwidth-usage network-id duration)})))