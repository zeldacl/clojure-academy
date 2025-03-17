(ns cn.academy.forge-test.integration-test
  (:require [clojure.test :refer :all]
            [cn.academy.core.config :as config]
            [cn.academy.core.util.monitoring :as monitoring]
            [cn.academy.core.block.block.block-cat-engine :as cat-engine]
            [cn.academy.core.network.messages :as messages])
  (:import [net.minecraft.util.math BlockPos]
           [net.minecraft.world World]
           [net.minecraftforge.energy IEnergyStorage]))

(defn create-mock-world []
  (reify World
    (dimension [_] 0)
    (provider [_] nil)))

(defn create-mock-pos []
  (BlockPos. 0 0 0))

(defn create-mock-energy-storage []
  (reify IEnergyStorage
    (receiveEnergy [_ amount _] amount)
    (extractEnergy [_ amount _] amount)
    (getEnergyStored [_] 1000)
    (getMaxEnergyStored [_] 5000)
    (canExtract [_] true)
    (canReceive [_] true)))

(deftest test-cat-engine-block
  (let [world (create-mock-world)
        pos (create-mock-pos)
        energy (create-mock-energy-storage)
        engine (cat-engine/create-cat-engine)]
    
    (testing "Block properties"
      (is (= "cat_engine" (.getRegistryName engine)))
      (is (.hasEnergyCapability engine)))
    
    (testing "Energy transfer"
      (let [energy-cap (.getEnergy engine world pos nil)]
        (is (= 100 (.receiveEnergy energy-cap 100 false)))
        (is (pos? (.getEnergyStored energy-cap)))))
    
    (testing "Network messages"
      (let [msg (messages/->CatEngineUpdateMessage pos 1000 true)]
        (is (= pos (:pos msg)))
        (is (= 1000 (:energy-stored msg)))
        (is (:linked? msg))))))

(deftest test-version-compatibility
  (testing "Configuration compatibility"
    (config/load-config! 
      {:cat-engine 
       {:energy-gen-rate 10.0
        :wireless-range 32
        :allow-interdimensional true}})
    
    (is (= 10.0 (config/get-config [:cat-engine :energy-gen-rate])))
    (is (= 32 (config/get-config [:cat-engine :wireless-range])))
    (is (true? (config/get-config [:cat-engine :allow-interdimensional]))))
  
  (testing "Performance monitoring across versions"
    (monitoring/reset-metrics!)
    (monitoring/with-timing "cross-version-test"
      (Thread/sleep 10))
    (let [metrics (monitoring/get-metrics)]
      (is (contains? metrics "cross-version-test")))))