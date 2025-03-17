(ns cn.academy.core.test.cat-engine-test
  (:require [clojure.test :refer :all]
            [cn.academy.core.config :as config]
            [cn.academy.core.util.monitoring :as monitoring]
            [cn.academy.core.util.serialization :as serial]
            [cn.academy.core.energy.chunk-cache :as cache])
  (:import [net.minecraft.nbt NBTTagCompound]))

(deftest test-energy-serialization
  (let [data {:stored 1000 :capacity 5000}
        storage (serial/deserialize-energy-data data 100)]
    (testing "Basic energy storage operations"
      (is (= 1000 (.getEnergyStored storage)))
      (is (= 5000 (.getMaxEnergyStored storage)))
      (is (true? (.canExtract storage)))
      (is (true? (.canReceive storage))))
    
    (testing "Energy extraction"
      (is (= 100 (.extractEnergy storage 200 false)))
      (is (= 900 (.getEnergyStored storage))))
    
    (testing "Energy reception"
      (is (= 100 (.receiveEnergy storage 200 false)))
      (is (= 1000 (.getEnergyStored storage))))))

(deftest test-chunk-cache
  (let [world (reify Object 
                (dimension [_] 0))
        cache (cache/get-or-create-cache world 0 0)]
    (testing "Cache operations"
      (cache/add-node! cache [0 0 0] "test-node")
      (is (= ["test-node"] (cache/get-nodes cache)))
      (cache/remove-node! cache [0 0 0])
      (is (empty? (cache/get-nodes cache))))))

(deftest test-configuration
  (testing "Default config values"
    (config/load-config! {})
    (is (= 5.0 (config/get-config [:cat-engine :energy-gen-rate] 5.0)))
    (is (= 16 (config/get-config [:cat-engine :wireless-range] 16))))
  
  (testing "Custom config values"
    (config/load-config! 
      {:cat-engine 
       {:energy-gen-rate 10.0
        :wireless-range 32}})
    (is (= 10.0 (config/get-config [:cat-engine :energy-gen-rate])))
    (is (= 32 (config/get-config [:cat-engine :wireless-range])))))

(deftest test-performance-monitoring
  (testing "Performance metrics"
    (monitoring/reset-metrics!)
    (monitoring/with-timing "test-op"
      (Thread/sleep 100))
    (let [metrics (monitoring/get-metrics)
          test-metric (get metrics "test-op")]
      (is (= 1 (:count test-metric)))
      (is (>= (:total-ms test-metric) 100)))))