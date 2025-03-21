(ns cn.academy.tech-system.energy-system.test.admin-test
  (:require [cn.academy.tech-system.energy-system.admin :as admin]
            [cn.academy.tech-system.energy-system.debug :as debug]
            [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.test.helpers :refer :all]
            [mcmod.test.player :as player-test]
            [clojure.test :refer :all]))

(defrecord TestCommandResult [feedback error])

(defn- create-test-player []
  (player-test/create-test-player "test-admin" true))

(defn- capture-command-output [f]
  (let [result (atom {:feedback [] :error nil})]
    (with-redefs [mcmod.commands/feedback (fn [_ msg] (swap! result update :feedback conj msg))
                  mcmod.commands/error (fn [_ msg] (swap! result assoc :error msg))]
      (f)
      (map->TestCommandResult @result))))

(deftest test-debug-commands
  (testing "Debug enable/disable commands"
    (let [player (create-test-player)]
      (let [result (capture-command-output #(admin/handle-debug player ["enable"]))]
        (is (= ["Debug logging enabled"] (:feedback result)))
        (is (:enabled @debug/debug-state)))
      
      (let [result (capture-command-output #(admin/handle-debug player ["disable"]))]
        (is (= ["Debug logging disabled"] (:feedback result)))
        (is (not (:enabled @debug/debug-state))))))

  (testing "Network dump command"
    (let [player (create-test-player)
          node (create-test-node {:x 0 :y 0 :z 0} :energy 1000)
          network (create-test-network node)]
      (debug/enable-debug!)
      (let [result (capture-command-output 
                    #(admin/handle-debug player ["dump" (:id network)]))]
        (is (= ["Network state dumped to log"] (:feedback result)))))))

(deftest test-network-commands
  (testing "Network list command"
    (let [player (create-test-player)
          node1 (create-test-node {:x 0 :y 0 :z 0})
          node2 (create-test-node {:x 8 :y 0 :z 0})
          net1 (create-test-network node1)
          net2 (create-test-network node2)]
      (let [result (capture-command-output 
                    #(admin/handle-network player ["list"]))]
        (is (= 2 (count (:feedback result)))))))
  
  (testing "Network info command"
    (let [player (create-test-player)
          node (create-test-node {:x 0 :y 0 :z 0} :energy 1000)
          network (create-test-network node)]
      (let [result (capture-command-output 
                    #(admin/handle-network player ["info" (:id network)]))]
        (is (= 3 (count (:feedback result))))
        (is (some #(.contains % "Total Energy: 1000") (:feedback result)))))))

(deftest test-security-commands
  (testing "Blacklist commands"
    (let [admin (create-test-player)
          target (create-test-player)]
      (let [result (capture-command-output 
                    #(admin/handle-security admin ["blacklist" "add" (.getName target)]))]
        (is (= ["Player added to blacklist"] (:feedback result))))
      
      (let [result (capture-command-output 
                    #(admin/handle-security admin ["blacklist" "remove" (.getName target)]))]
        (is (= ["Player removed from blacklist"] (:feedback result)))))))

(deftest test-command-registration
  (testing "Command registration"
    (admin/register-commands!)
    (let [player (create-test-player)
          result (capture-command-output 
                  #(admin/handle-debug player ["enable"]))]
      (is (= ["Debug logging enabled"] (:feedback result)))
      (is (nil? (:error result))))

    (let [player (player-test/create-test-player "normal-player" false)
          result (capture-command-output 
                  #(admin/handle-debug player ["enable"]))]
      (is (some? (:error result)) "Should error for non-op player"))))

(deftest test-analytics-commands
  (testing "Network report command"
    (let [player (create-test-player)]
      (let [result (capture-command-output 
                    #(admin/handle-analytics player ["report" "test-network"]))]
        (is (some #(.contains % "Network Report for test-network") (:feedback result)))
        (is (some #(.contains % "Daily Statistics:") (:feedback result)))
        (is (some #(.contains % "Recommendations:") (:feedback result))))))
  
  (testing "Network trends command"
    (let [player (create-test-player)]
      (let [result (capture-command-output 
                    #(admin/handle-analytics player ["trends" "test-network"]))]
        (is (some #(.contains % "Network Trends for test-network") (:feedback result)))
        (is (some #(.contains % "Energy Trend:") (:feedback result)))
        (is (some #(.contains % "Stability Score:") (:feedback result)))
        (is (some #(.contains % "Efficiency:") (:feedback result))))))
  
  (testing "Usage prediction command"
    (let [player (create-test-player)]
      ;; Test default hours
      (let [result (capture-command-output 
                    #(admin/handle-analytics player ["predict" "test-network"]))]
        (is (some #(.contains % "Energy Predictions for test-network") (:feedback result)))
        (is (some #(.contains % "Reliability:") (:feedback result)))
        (is (<= 5 (count (filter #(.contains % "Hour") (:feedback result)))))
        
      ;; Test custom hours
      (let [result (capture-command-output 
                    #(admin/handle-analytics player ["predict" "test-network" "12"]))]
        (is (some #(.contains % "Energy Predictions for test-network") (:feedback result)))
        (is (some #(.contains % "Reliability:") (:feedback result)))
        (is (<= 5 (count (filter #(.contains % "Hour") (:feedback result)))))))))

(deftest test-analytics-error-handling
  (testing "Invalid network ID handling"
    (let [player (create-test-player)]
      (doseq [cmd ["report" "trends" "predict"]]
        (let [result (capture-command-output 
                      #(admin/handle-analytics player [cmd]))]
          (is (some? (:error result)) "Should error when network ID is missing")))))
  
  (testing "Invalid prediction hours handling"
    (let [player (create-test-player)]
      (let [result (capture-command-output 
                    #(admin/handle-analytics player ["predict" "test-network" "invalid"]))]
        (is (some? (:error result)) "Should handle invalid hours parameter")))))