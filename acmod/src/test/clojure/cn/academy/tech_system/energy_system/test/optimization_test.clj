(ns cn.academy.tech-system.energy-system.test.optimization-test
  (:require [cn.academy.tech-system.energy-system.optimization :as optimization]
            [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.test.helpers :refer :all]
            [clojure.test :refer :all]))

(deftest test-network-optimization
  (testing "Basic network optimization"
    (let [optimizer (optimization/->NetworkOptimizer 
                     (atom {:optimization-enabled true
                           :auto-balance-threshold 0.25}))
          node1 (create-test-node {:x 0 :y 0 :z 0} 
                                :energy 2000
                                :max-energy 2000)
          node2 (create-test-node {:x 8 :y 0 :z 0}
                                :energy 0
                                :max-energy 2000)
          network (create-test-network node1 node2)]
      (is (optimization/optimize-network! optimizer network)
          "Optimization should succeed")
      ;; Check energy balance after optimization
      (let [energy1 (network/get-energy node1)
            energy2 (network/get-energy node2)]
        (is (< (Math/abs (- energy1 energy2)) 500)
            "Energy should be roughly balanced"))))
  
  (testing "Connection optimization"
    (let [optimizer (optimization/->NetworkOptimizer
                     (atom {:optimization-enabled true
                           :auto-balance-threshold 0.25}))
          node1 (create-test-node {:x 0 :y 0 :z 0})
          node2 (create-test-node {:x 8 :y 0 :z 0})
          node3 (create-test-node {:x 50 :y 0 :z 0}) ; Out of efficient range
          network (create-test-network node1 node2 node3)]
      (optimization/optimize-connections! optimizer network)
      (is (network/connected? node1 node2) "Close nodes should remain connected")
      (is (not (network/connected? node1 node3)) 
          "Inefficient connections should be removed"))))

(deftest test-load-balancing
  (testing "Network load balancing"
    (let [optimizer (optimization/->NetworkOptimizer
                     (atom {:optimization-enabled true
                           :auto-balance-threshold 0.25}))
          nodes (for [i (range 3)]
                 (create-test-node {:x (* i 8) :y 0 :z 0}
                                 :energy (* i 1000)
                                 :max-energy 3000))
          network (apply create-test-network nodes)]
      (optimization/balance-network-load! optimizer network)
      (let [energies (map network/get-energy nodes)
            avg (/ (reduce + energies) (count energies))
            max-diff (apply max (map #(Math/abs (- % avg)) energies))]
        (is (< (/ max-diff avg) 0.25)
            "Energy difference should be below threshold")))))

(deftest test-improvement-suggestions
  (testing "Network improvement suggestions"
    (let [optimizer (optimization/->NetworkOptimizer
                     (atom {:optimization-enabled true
                           :auto-balance-threshold 0.25}))
          network (create-test-network)]
      (let [suggestions (:suggestions 
                         (optimization/suggest-network-improvements 
                           optimizer network))]
        (is (some #(= :add-nodes (:type %)) suggestions)
            "Should suggest adding nodes to empty network"))))
  
  (testing "Node upgrade suggestions"
    (let [optimizer (optimization/->NetworkOptimizer
                     (atom {:optimization-enabled true
                           :auto-balance-threshold 0.25}))
          node (create-test-node {:x 0 :y 0 :z 0}
                               :energy 1900
                               :max-energy 2000)
          network (create-test-network node)]
      (let [suggestions (:suggestions 
                         (optimization/suggest-network-improvements
                           optimizer network))]
        (is (some #(and (= :upgrade-node (:type %))
                       (= :medium (:priority %)))
                 suggestions)
            "Should suggest upgrading nearly full nodes")))))