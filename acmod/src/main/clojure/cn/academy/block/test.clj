(ns cn.academy.block.test
  (:require [cn.academy.block.core :as core]
            [cn.academy.block.validation :as validation]
            [cn.academy.block.error :as error]
            [cn.academy.block.debug :as debug]
            [clojure.test :refer :all]
            [clojure.tools.logging :as log]))

;; Mock world implementation
(defprotocol IMockWorld
  (add-block! [this pos block])
  (remove-block! [this pos])
  (get-block-at [this pos]))

(defrecord MockWorld [blocks]
  IMockWorld
  (add-block! [_ pos block]
    (swap! blocks assoc pos block))
  (remove-block! [_ pos]
    (swap! blocks dissoc pos))
  (get-block-at [_ pos]
    (get @blocks pos)))

(defn create-mock-world []
  (->MockWorld (atom {})))

;; Test helpers
(defn with-test-block [block-type config test-fn]
  (let [world (create-mock-world)
        pos {:x 0 :y 0 :z 0}
        block (core/create-block block-type config)]
    (add-block! world pos block)
    (test-fn world pos block)))

(defn verify-state! [block expected-state]
  (let [current-state @(:state block)]
    (every? (fn [[k v]]
              (= (get current-state k) v))
            expected-state)))

;; Block test definitions
(defn test-block-creation! [block-type config]
  (testing (str "Block creation for " block-type)
    (let [block (core/create-block block-type config)]
      (is (some? block) "Block should be created")
      (is (validation/validate-machine! block) "Block should be valid")
      (is (= block-type (:type block)) "Block should have correct type"))))

(defn test-block-placement! [block-type config]
  (with-test-block block-type config
    (fn [world pos block]
      (testing (str "Block placement for " block-type)
        (is (some? (get-block-at world pos)) "Block should be in world")
        (is (core/can-place? block pos) "Block should be placeable")))))

(defn test-block-removal! [block-type config]
  (with-test-block block-type config
    (fn [world pos block]
      (testing (str "Block removal for " block-type)
        (is (core/can-remove? block pos) "Block should be removable")
        (remove-block! world pos)
        (is (nil? (get-block-at world pos)) "Block should be removed from world")))))

;; Energy system tests
(defn test-energy-handling! [block-type config]
  (with-test-block block-type config
    (fn [world pos block]
      (testing (str "Energy handling for " block-type)
        (let [energy-cap (get-in block [:config :energy-capacity])
              test-amount (quot energy-cap 2)]
          (swap! (:state block) assoc :energy test-amount)
          (is (= test-amount (get-in @(:state block) [:energy]))
              "Energy should be stored correctly"))))))

;; Recipe system tests
(defn test-recipe-processing! [block-type config recipe]
  (with-test-block block-type config
    (fn [world pos block]
      (testing (str "Recipe processing for " block-type)
        (let [initial-energy (get-in config [:energy-capacity] 0)]
          (swap! (:state block) assoc 
                 :energy initial-energy
                 :current-recipe recipe)
          (is (validation/can-process? block recipe)
              "Should be able to process recipe"))))))

;; Run all tests
(defn run-block-tests! [block-type config]
  (try
    (debug/enable-debug!)
    (test-block-creation! block-type config)
    (test-block-placement! block-type config)
    (test-block-removal! block-type config)
    (test-energy-handling! block-type config)
    (log/info "All tests passed for block type:" block-type)
    true
    (catch Exception e
      (log/error "Tests failed for block type:" block-type (.getMessage e))
      false)
    (finally
      (debug/disable-debug!))))