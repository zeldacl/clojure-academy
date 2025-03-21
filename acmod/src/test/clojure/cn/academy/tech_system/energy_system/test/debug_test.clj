(ns cn.academy.tech-system.energy-system.test.debug-test
  (:require [cn.academy.tech-system.energy-system.debug :as debug]
            [cn.academy.tech-system.energy-system.test.helpers :refer :all]
            [clojure.test :refer :all]))

(deftest test-debug-logging
  (testing "Debug state management"
    (debug/enable-debug!)
    (is (:enabled @debug/debug-state) "Debug should be enabled")
    (debug/disable-debug!)
    (is (not (:enabled @debug/debug-state)) "Debug should be disabled"))
  
  (testing "Energy transfer logging"
    (debug/enable-debug!)
    (debug/clear-history!)
    (let [source (create-test-node {:x 0 :y 0 :z 0})
          target (create-test-node {:x 8 :y 0 :z 0})]
      (debug/log-energy-transfer! source target 1000 true)
      (let [history (debug/get-debug-history :energy-transfer)]
        (is (= 1 (count history)) "Should have one energy transfer log")
        (is (= 1000 (:amount (first history))) "Should log correct amount")
        (is (:success (first history)) "Should log success status"))))

  (testing "Network change logging"
    (debug/enable-debug!)
    (debug/clear-history!)
    (let [network (create-test-network)
          node (create-test-node {:x 0 :y 0 :z 0})]
      (debug/log-network-change! network :node-added {:node-id (:id node)})
      (let [history (debug/get-debug-history :network-change)]
        (is (= 1 (count history)) "Should have one network change log")
        (is (= :node-added (:event (first history))) "Should log correct event type")
        (is (= (:id network) (:network-id (first history))) "Should log network ID"))))

  (testing "Security event logging"
    (debug/enable-debug!)
    (debug/clear-history!)
    (let [node (create-test-node {:x 0 :y 0 :z 0})
          player (reify Object
                  (getUniqueID [_] "test-player-id"))]
      (debug/log-security-event! node player :access-attempt true)
      (let [history (debug/get-debug-history :security-event)]
        (is (= 1 (count history)) "Should have one security event log")
        (is (= :access-attempt (:event (first history))) "Should log correct event type")
        (is (:result (first history)) "Should log access result"))))

  (testing "Network state dumping"
    (debug/enable-debug!)
    (let [node1 (create-test-node {:x 0 :y 0 :z 0} :energy 1000)
          node2 (create-test-node {:x 8 :y 0 :z 0} :energy 500)
          network (create-test-network node1 node2)]
      ;; Just verify it doesn't throw - actual output goes to log
      (is (nil? (debug/dump-network-state! network)) "Should dump network state without error"))))