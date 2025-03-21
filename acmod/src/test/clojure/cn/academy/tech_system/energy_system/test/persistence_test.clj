(ns cn.academy.tech-system.energy-system.test.persistence-test
  (:require [cn.academy.tech-system.energy-system.persistence :as persistence]
            [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.test.helpers :refer :all]
            [mcmod.nbt :as nbt]
            [mcmod.world :as world]
            [clojure.test :refer :all]))

(use-fixtures :each with-test-env)

(deftest test-network-persistence
  (testing "Saving and loading single network"
    (let [world (create-test-world)
          node1 (create-test-node {:x 0 :y 0 :z 0} :energy 1000)
          node2 (create-test-node {:x 8 :y 0 :z 0} :energy 500)
          network (create-test-network node1 node2)]
      ;; Save network
      (persistence/save-network! persistence/storage world (:id network))
      ;; Clear current state
      (network/remove-node! network node1)
      (network/remove-node! network node2)
      ;; Load network and verify
      (let [loaded-net (persistence/load-network! persistence/storage world (:id network))
            loaded-nodes (network/get-nodes loaded-net)]
        (is (= 2 (count loaded-nodes)) "Should restore both nodes")
        (is (some #(= 1000 (network/get-energy %)) loaded-nodes) "Should restore energy values")
        (is (some #(= 500 (network/get-energy %)) loaded-nodes) "Should restore energy values"))))

  (testing "Saving and loading multiple networks"
    (let [world (create-test-world)
          node1 (create-test-node {:x 0 :y 0 :z 0})
          node2 (create-test-node {:x 8 :y 0 :z 0})
          node3 (create-test-node {:x 16 :y 0 :z 0})
          net1 (create-test-network node1)
          net2 (create-test-network node2 node3)]
      ;; Save all networks
      (persistence/save-all! persistence/storage world)
      ;; Clear current state
      (network/remove-node! net1 node1)
      (network/remove-node! net2 node2)
      (network/remove-node! net2 node3)
      ;; Load all networks and verify
      (persistence/load-all! persistence/storage world)
      (let [networks (network/get-all-networks)
            total-nodes (reduce + (map #(count (network/get-nodes %)) (vals networks)))]
        (is (= 3 total-nodes) "Should restore all nodes across networks")))))

(deftest test-security-persistence
  (testing "Saving and loading security state"
    (let [world (create-test-world)
          manager (security/create-security-manager)
          player (reify Object 
                  (getUniqueID [_] "test-player-id"))]
      ;; Set up security state
      (security/add-to-blacklist manager player)
      ;; Save state
      (let [data (nbt/create-compound)]
        (security/save-security-state! data)
        (world/set-world-data! world "energy_security" data))
      ;; Clear current state
      (security/remove-from-blacklist manager player)
      ;; Load state and verify
      (when-let [loaded-data (world/get-world-data world "energy_security")]
        (security/load-security-state! loaded-data)
        (is (not (security/check-access manager 
                                      (create-test-node {:x 0 :y 0 :z 0})
                                      player 
                                      nil))
            "Should restore blacklisted status")))))