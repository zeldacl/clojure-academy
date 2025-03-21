(ns cn.academy.tech-system.energy-system.test.monitoring-test
  (:require [cn.academy.tech-system.energy-system.monitoring :as monitoring]
            [cn.academy.tech-system.energy-system.test.helpers :refer :all]
            [clojure.test :refer :all]))

(deftest test-metrics-collection
  (testing "Basic metrics collection"
    (let [monitor (monitoring/->NetworkMonitor 
                   (atom {:metrics {} :alerts [] 
                         :thresholds (:thresholds @monitoring/monitor-state)}))
          node1 (create-test-node {:x 0 :y 0 :z 0} 
                                :energy 1000 
                                :max-energy 2000)
          node2 (create-test-node {:x 8 :y 0 :z 0}
                                :energy 500
                                :max-energy 2000)
          network (create-test-network node1 node2)]
      (let [metrics (monitoring/collect-metrics! monitor network)]
        (is (= 2 (:node-count metrics)) "Should count all nodes")
        (is (= 1500 (:total-energy metrics)) "Should sum energy correctly")
        (is (= 750 (:avg-energy metrics)) "Should calculate average energy"))))
  
  (testing "Metrics history"
    (let [monitor (monitoring/->NetworkMonitor 
                   (atom {:metrics {} :alerts []
                         :thresholds (:thresholds @monitoring/monitor-state)}))
          node (create-test-node {:x 0 :y 0 :z 0} :energy 1000)
          network (create-test-network node)]
      ;; Collect metrics multiple times
      (dotimes [_ 3]
        (monitoring/collect-metrics! monitor network)
        (Thread/sleep 10)) ; Ensure different timestamps
      (let [history (monitoring/get-metrics-history monitor (:id network) :energy)]
        (is (= 3 (count history)) "Should store metrics history")
        (is (every? #(contains? % :total-energy) history) "Should track energy metrics")))))

(deftest test-alert-generation
  (testing "Low energy alerts"
    (let [monitor (monitoring/->NetworkMonitor 
                   (atom {:metrics {} :alerts []
                         :thresholds {:energy-critical 0.1}}))
          node (create-test-node {:x 0 :y 0 :z 0} 
                               :energy 50 
                               :max-energy 1000)
          network (create-test-network node)]
      (monitoring/check-alerts! monitor network)
      (let [alerts (:alerts @(:state-atom monitor))]
        (is (= 1 (count alerts)) "Should generate alert for low energy")
        (is (= :energy-critical (:type (first alerts))) "Should identify alert type"))))
  
  (testing "Connection overload alerts"
    (let [monitor (monitoring/->NetworkMonitor 
                   (atom {:metrics {} :alerts []
                         :thresholds {:connection-high 0.8}}))
          node (create-test-node {:x 0 :y 0 :z 0} 
                               :connections 5
                               :max-connections 5)
          network (create-test-network node)]
      (monitoring/check-alerts! monitor network)
      (let [alerts (:alerts @(:state-atom monitor))]
        (is (some #(= :connection-overload (:type %)) alerts)
            "Should alert on connection overload")))))

(deftest test-network-health
  (testing "Health status calculation"
    (let [monitor (monitoring/->NetworkMonitor 
                   (atom {:metrics {} :alerts []
                         :thresholds {:energy-low 0.2
                                    :energy-critical 0.1}}))
          cases [{:energy 1000 :max-energy 1000 :expected :healthy}
                {:energy 150 :max-energy 1000 :expected :warning}
                {:energy 50 :max-energy 1000 :expected :critical}]]
      (doseq [{:keys [energy max-energy expected]} cases]
        (let [node (create-test-node {:x 0 :y 0 :z 0} 
                                   :energy energy
                                   :max-energy max-energy)
              network (create-test-network node)
              health (monitoring/get-network-health monitor network)]
          (is (= expected (:status health))
              (str "Health status should be " expected 
                   " for energy ratio " (double (/ energy max-energy))))))
      
      ;; Test offline status
      (let [network (create-test-network)
            health (monitoring/get-network-health monitor network)]
        (is (= :offline (:status health))
            "Empty network should be marked as offline")))))