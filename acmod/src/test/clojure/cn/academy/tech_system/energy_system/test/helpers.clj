(ns cn.academy.tech-system.energy-system.test.helpers
  (:require [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [mcmod.test.world :as test-world]
            [clojure.test :refer :all]))

(defn create-test-world []
  (test-world/create-test-world))

(defn create-test-node
  "Create a test node with given position and properties"
  [pos & {:keys [energy max-energy range connections]
          :or {energy 0
               max-energy 5000
               range 8
               connections 4}}]
  (let [node-id (str (random-uuid))]
    (reify wireless/IWirelessNode
      (get-id [_] node-id)
      (get-position [_] pos)
      (get-energy [_] energy)
      (get-max-energy [_] max-energy)
      (get-range [_] range)
      (get-capacity [_] connections)
      (is-active? [_] true)
      (can-receive? [_] true)
      (can-extract? [_] true))))

(defn create-test-network
  "Create a test network with given nodes"
  [& nodes]
  (let [net (network/create-network!)]
    (doseq [node nodes]
      (network/add-node! net node))
    net))

(defn assert-network-state
  "Assert that network state matches expectations"
  [net expected]
  (let [nodes (network/get-nodes net)]
    (is (= (count nodes) (count expected))
        "Network should have expected number of nodes")
    (doseq [[node {:keys [energy]}] (map vector nodes expected)]
      (is (= (wireless/get-energy node) energy)
          (str "Node " (wireless/get-id node) " should have expected energy")))))

(defn with-test-env
  "Run test with clean environment"
  [f]
  (let [world (create-test-world)]
    (try
      (f world)
      (finally
        (test-world/cleanup! world)))))