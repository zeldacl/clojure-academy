(ns cn.academy.tech-system.energy-system.test.error-handling-test
  (:require [cn.academy.tech-system.energy-system.error-handling :as error]
            [cn.academy.tech-system.energy-system.test.helpers :refer :all]
            [clojure.test :refer :all]))

(deftest test-error-handling
  (testing "Basic error handling"
    (let [handler (error/->NetworkErrorHandler (atom {:errors {} :warnings {} :last-error nil}))]
      (error/handle-error! handler (Exception. "Test error") {:test "context"})
      (let [last-error (error/get-last-error handler)]
        (is (not (nil? last-error)) "Should have last error")
        (is (= "Test error" (.getMessage (:error last-error))) "Error message should match")
        (is (= {:test "context"} (:context last-error)) "Context should be preserved"))))
  
  (testing "Warning handling"
    (let [handler (error/->NetworkErrorHandler (atom {:errors {} :warnings {} :last-error nil}))
          warning-id (error/handle-warning! handler "Test warning" {:test "context"})]
      (is (not (nil? warning-id)) "Should return warning ID")
      (let [report (error/get-error-report)]
        (is (= 0 (:total-errors report)) "Should have no errors")
        (is (= 1 (:total-warnings report)) "Should have one warning"))))
  
  (testing "Error clearing"
    (let [handler (error/->NetworkErrorHandler (atom {:errors {} :warnings {} :last-error nil}))]
      (error/handle-error! handler (Exception. "Test error") {})
      (error/handle-warning! handler "Test warning" {})
      (error/clear-errors! handler)
      (let [report (error/get-error-report)]
        (is (= 0 (:total-errors report)) "Should have no errors after clear")
        (is (= 0 (:total-warnings report)) "Should have no warnings after clear")
        (is (nil? (:last-error report)) "Should have no last error after clear")))))

(deftest test-error-handling-macros
  (testing "with-error-handling macro"
    (let [handler (error/->NetworkErrorHandler (atom {:errors {} :warnings {} :last-error nil}))
          result (error/with-error-handling handler {:operation "test"}
                  (throw (Exception. "Test error")))]
      (is (nil? result) "Should return nil on error")
      (let [last-error (error/get-last-error handler)]
        (is (= "Test error" (.getMessage (:error last-error))) "Should capture thrown error"))))
  
  (testing "with-network-safety macro"
    (let [handler (error/->NetworkErrorHandler (atom {:errors {} :warnings {} :last-error nil}))
          network (reify 
                   Object 
                   (toString [_] "test-network")
                   protocols/IValidatable
                   (valid? [_] false))]
      (error/with-network-safety handler network
        (throw (Exception. "Should not reach")))
      (let [report (error/get-error-report)]
        (is (= 0 (:total-errors report)) "Should not record error for invalid network")
        (is (= 1 (:total-warnings report)) "Should record warning for invalid network")))))

(deftest test-error-filtering
  (testing "Filtering errors by timestamp"
    (let [handler (error/->NetworkErrorHandler (atom {:errors {} :warnings {} :last-error nil}))
          start-time (System/currentTimeMillis)]
      (Thread/sleep 10) ; Ensure timestamp difference
      (error/handle-error! handler (Exception. "Test error 1") {})
      (error/handle-error! handler (Exception. "Test error 2") {})
      (let [errors (error/get-errors-since start-time)]
        (is (= 2 (count errors)) "Should find all errors after start time")
        (is (every? #(> (:timestamp %) start-time) errors) "All errors should be after start time")))))