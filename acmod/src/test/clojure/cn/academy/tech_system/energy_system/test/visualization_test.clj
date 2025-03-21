(ns cn.academy.tech-system.energy-system.test.visualization-test
  (:require [cn.academy.tech-system.energy-system.visualization :as viz]
            [clojure.test :refer :all]
            [clojure.string :as str]))

(deftest test-line-chart
  (testing "Basic line chart generation"
    (let [values [1 2 3 2 1]
          chart (viz/create-line-chart "Test Chart" values 10 3)]
      (is (str/includes? chart "Test Chart") "Should include title")
      (is (= 5 (count (str/split-lines chart))) "Should have correct height")
      (is (str/includes? chart "└") "Should have bottom border")
      (is (str/includes? chart "┌") "Should have top border")))
  
  (testing "Line chart with labels"
    (let [values [1 2 3 4 5]
          chart (viz/create-line-chart "Test" values 10 3 :labels ["A" "B"])]
      (is (str/includes? chart "A") "Should include first label")
      (is (str/includes? chart "B") "Should include second label")))
  
  (testing "Value normalization"
    (let [values [10 20 30 40 50]
          chart (viz/create-line-chart "Test" values 10 3)]
      (is (str/includes? chart "█") "Should contain full blocks")
      (is (> (count (re-seq #"█" chart)) 3) "Should have multiple points"))))

(deftest test-bar-chart
  (testing "Basic bar chart generation"
    (let [categories ["A" "B" "C"]
          values [1 2 3]
          chart (viz/create-bar-chart "Test Chart" categories values 15 4)]
      (is (str/includes? chart "Test Chart") "Should include title")
      (is (every? #(str/includes? chart %) categories) "Should include all categories")
      (is (str/includes? chart "█") "Should contain bar blocks")))
  
  (testing "Bar height scaling"
    (let [categories ["X" "Y"]
          values [10 20]
          chart (viz/create-bar-chart "Test" categories values 10 4)
          lines (str/split-lines chart)]
      (is (= 6 (count lines)) "Should have correct number of lines")
      (is (> (count (re-seq #"█" (nth lines 3)))
             (count (re-seq #"█" (nth lines 2))))
          "Taller value should have more blocks")))
  
  (testing "Long category names"
    (let [categories ["Very Long Name" "Also Long"]
          values [1 1]
          chart (viz/create-bar-chart "Test" categories values 20 3)]
      (is (not (str/includes? chart "Very Long Name")) "Should truncate long category names")
      (is (str/includes? chart "..") "Should indicate truncation"))))

(deftest test-legend-formatting
  (testing "Basic legend formatting"
    (let [items [["A" 10] ["B" 20]]
          legend (viz/format-legend items 20)]
      (is (str/includes? legend "A: 10") "Should format first item")
      (is (str/includes? legend "B: 20") "Should format second item")))
  
  (testing "Width limiting"
    (let [items [["Long" 100] ["Also Long" 200]]
          legend (viz/format-legend items 15)]
      (is (<= (count legend) 15) "Should respect width limit"))))