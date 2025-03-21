(ns cn.academy.tech-system.energy-system.test.security-test
  (:require [cn.academy.tech-system.energy-system.security :as security]
            [cn.academy.tech-system.energy-system.test.helpers :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each with-test-env)

(deftest test-security-access
  (testing "Basic access control"
    (let [manager (security/create-security-manager)
          node (create-test-node {:x 0 :y 0 :z 0})
          player (reify Object 
                  (getUniqueID [_] "test-player-id"))]
      (is (security/check-access manager node player nil)
          "Should allow access with no password")))
  
  (testing "Password protection"
    (let [manager (security/create-security-manager)
          node (assoc (create-test-node {:x 0 :y 0 :z 0})
                     :password-hash (hash "secret"))
          player (reify Object 
                  (getUniqueID [_] "test-player-id"))]
      (is (not (security/check-access manager node player "wrong"))
          "Should deny access with wrong password")
      (is (security/check-access manager node player "secret")
          "Should allow access with correct password"))))

(deftest test-blacklisting
  (testing "Player blacklisting"
    (let [manager (security/create-security-manager)
          node (create-test-node {:x 0 :y 0 :z 0})
          player (reify Object 
                  (getUniqueID [_] "test-player-id"))]
      (security/add-to-blacklist manager player)
      (is (not (security/check-access manager node player nil))
          "Should deny access to blacklisted player")
      
      (security/remove-from-blacklist manager player)
      (is (security/check-access manager node player nil)
          "Should allow access after removing from blacklist"))))

(deftest test-access-logging
  (testing "Access attempt logging"
    (let [manager (security/create-security-manager)
          node (assoc (create-test-node {:x 0 :y 0 :z 0}) :id "test-node")
          player (reify Object 
                  (getUniqueID [_] "test-player-id"))]
      (security/log-access-attempt manager node player true)
      (let [logs (get-in @(:state-atom manager) [:access-logs "test-node"])]
        (is (= 1 (count logs)) "Should have one log entry")
        (is (= "test-player-id" (:player (first logs))) "Log should contain player ID")
        (is (:success (first logs)) "Log should indicate success")))))