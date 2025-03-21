(ns cn.academy.tech-system.energy-system.test.analytics-test
  (:require [cn.academy.tech-system.energy-system.analytics :as analytics]
            [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.test.helpers :refer :all]
            [clojure.test :refer :all]))

(deftest test-stats-collection
  (testing "Basic statistics collection"
    (let [analyzer (analytics/->NetworkAnalytics 
                    (atom {:stats {} :hourly-snapshots [] :daily-summaries []
                          :retention-days 7}))
          node1 (create-test-node {:x 0 :y 0 :z 0} :energy 1000)
          node2 (create-test-node {:x 8 :y 0 :z 0} :energy 500)
          network (create-test-network node1 node2)
          timestamp (System/currentTimeMillis)
          stats (analytics/collect-stats! analyzer network timestamp)]
      (is (= 2 (:node-count stats)) "Should count all nodes")
      (is (= 1500 (:total-energy stats)) "Should sum total energy")
      (is (= 750 (:avg-energy stats)) "Should calculate average energy")))

  (testing "Statistics retention"
    (let [analyzer (analytics/->NetworkAnalytics 
                    (atom {:stats {} :hourly-snapshots [] :daily-summaries []
                          :retention-days 1}))
          node (create-test-node {:x 0 :y 0 :z 0} :energy 1000)
          network (create-test-network node)]
      ;; Collect more stats than retention limit
      (dotimes [i 30]
        (analytics/collect-stats! analyzer network 
                                (+ (System/currentTimeMillis) (* i 3600000))))
      (let [stats (get-in @(:state-atom analyzer) [:stats (:id network)])]
        (is (<= (count stats) 24) "Should maintain retention limit")))))

(deftest test-trend-analysis
  (testing "Energy trend analysis"
    (let [analyzer (analytics/->NetworkAnalytics 
                    (atom {:stats {} :hourly-snapshots [] :daily-summaries []
                          :retention-days 7}))
          node (create-test-node {:x 0 :y 0 :z 0} :energy 1000)
          network (create-test-network node)
          base-time (System/currentTimeMillis)]
      ;; Simulate increasing energy trend
      (doseq [i (range 5)]
        (analytics/collect-stats! analyzer network 
                                (+ base-time (* i 3600000))))
      (let [trends (analytics/analyze-trends analyzer (:id network) 5)]
        (is (contains? trends :energy-trend) "Should include energy trend analysis")
        (is (contains? trends :stability) "Should include stability analysis")
        (is (contains? trends :efficiency) "Should include efficiency analysis"))))
  
  (testing "Network stability calculation"
    (let [analyzer (analytics/->NetworkAnalytics 
                    (atom {:stats {} :hourly-snapshots [] :daily-summaries []
                          :retention-days 7}))
          node (create-test-node {:x 0 :y 0 :z 0} :energy 1000)
          network (create-test-network node)
          base-time (System/currentTimeMillis)]
      ;; Simulate stable energy levels
      (doseq [i (range 5)]
        (analytics/collect-stats! analyzer network 
                                (+ base-time (* i 3600000))))
      (let [trends (analytics/analyze-trends analyzer (:id network) 5)]
        (is (>= (:score (:stability trends)) 0.9)
            "Should have high stability score for constant energy")))))

(deftest test-report-generation
  (testing "Network report generation"
    (let [analyzer (analytics/->NetworkAnalytics 
                    (atom {:stats {} :hourly-snapshots [] :daily-summaries []
                          :retention-days 7}))
          node (create-test-node {:x 0 :y 0 :z 0} :energy 1000)
          network (create-test-network node)]
      ;; Collect some stats
      (dotimes [i 24]
        (analytics/collect-stats! analyzer network 
                                (+ (System/currentTimeMillis) (* i 3600000))))
      (let [report (analytics/generate-report analyzer (:id network))]
        (is (contains? report :current-state) "Should include current state")
        (is (contains? report :trends) "Should include trend analysis")
        (is (contains? report :daily-statistics) "Should include daily statistics")
        (is (vector? (:recommendations report)) "Should include recommendations")))))

(deftest test-usage-prediction
  (testing "Energy usage prediction"
    (let [analyzer (analytics/->NetworkAnalytics 
                    (atom {:stats {} :hourly-snapshots [] :daily-summaries []
                          :retention-days 7}))
          node (create-test-node {:x 0 :y 0 :z 0} :energy 1000)
          network (create-test-network node)]
      ;; Collect a week of simulated data
      (doseq [i (range (* 24 7))]
        (analytics/collect-stats! analyzer network 
                                (+ (System/currentTimeMillis) (* i 3600000))))
      (let [prediction (analytics/predict-usage analyzer (:id network) 24)]
        (is (contains? prediction :predictions) "Should include hourly predictions")
        (is (contains? prediction :reliability) "Should include prediction reliability")
        (is (= 24 (count (:predictions prediction))) "Should predict requested hours")
        (is (every? #(<= 0 (:confidence %) 1) (:predictions prediction))
            "Confidence scores should be between 0 and 1")))))