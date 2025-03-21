(ns cn.academy.tech-system.energy-system.test.core-test
  (:require [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.network.messaging :as msg]
            [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.test.helpers :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each with-test-env)

(deftest test-network-creation
  (testing "Creating an empty network"
    (let [net (network/create-network!)]
      (is (not (nil? (:id net))) "Network should have an ID")
      (is (empty? (network/get-nodes net)) "New network should have no nodes")))
  
  (testing "Adding nodes to network"
    (let [node1 (create-test-node {:x 0 :y 0 :z 0})
          node2 (create-test-node {:x 8 :y 0 :z 0})
          net (create-test-network node1 node2)]
      (is (= 2 (count (network/get-nodes net))) "Network should have two nodes")
      (is (network/connected? node1 node2) "Nodes should be connected"))))

(deftest test-energy-distribution
  (testing "Equal energy distribution"
    (let [node1 (create-test-node {:x 0 :y 0 :z 0} :energy 1000)
          node2 (create-test-node {:x 8 :y 0 :z 0} :energy 0)
          net (create-test-network node1 node2)]
      (network/balance-energy! net)
      (assert-network-state net [{:energy 500} {:energy 500}])))
  
  (testing "Energy distribution with capacity limits"
    (let [node1 (create-test-node {:x 0 :y 0 :z 0} :energy 2000 :max-energy 1000)
          node2 (create-test-node {:x 8 :y 0 :z 0} :energy 0 :max-energy 1000)
          net (create-test-network node1 node2)]
      (network/balance-energy! net)
      (assert-network-state net [{:energy 1000} {:energy 1000}]))))

(deftest test-network-merging
  (testing "Merging two networks"
    (let [node1 (create-test-node {:x 0 :y 0 :z 0})
          node2 (create-test-node {:x 8 :y 0 :z 0})
          node3 (create-test-node {:x 16 :y 0 :z 0})
          net1 (create-test-network node1)
          net2 (create-test-network node2 node3)]
      (network/merge-networks! net1 net2)
      (is (= 3 (count (network/get-nodes net1))) "Merged network should have all nodes")
      (is (empty? (network/get-nodes net2)) "Second network should be empty after merge"))))

(deftest test-network-splitting
  (testing "Splitting network when connection lost"
    (let [node1 (create-test-node {:x 0 :y 0 :z 0})
          node2 (create-test-node {:x 8 :y 0 :z 0})
          node3 (create-test-node {:x 50 :y 0 :z 0}) ; Out of range
          net (create-test-network node1 node2 node3)]
      (network/validate-network! net)
      (is (= 2 (count (network/get-connected-nodes node1))) "Node1 should have one connection")
      (is (= 1 (count (network/get-connected-nodes node3))) "Node3 should be isolated"))))