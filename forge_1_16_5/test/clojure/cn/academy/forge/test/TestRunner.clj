(ns cn.academy.forge.test.TestRunner
  (:require [clojure.test :refer :all]
            [cn.academy.forge.test.adapter-test]
            [cn.academy.forge.test.network-test]
            [cn.academy.forge.test.event-test]
            [cn.academy.forge.test.datagen-test]
            [cn.academy.forge.test.render-test])
  (:import [net.minecraftforge.fml.loading FMLLoader]
           [net.minecraftforge.fml ModLoadingContext])
  (:gen-class))

(defn setup-test-environment! []
  ;; Initialize minimal Forge test environment
  (when-not (FMLLoader/isProduction)
    (ModLoadingContext/get)))

(defn run-all-tests []
  (setup-test-environment!)
  
  ;; Run all test namespaces
  (run-tests
    'cn.academy.forge.test.adapter-test
    'cn.academy.forge.test.network-test  
    'cn.academy.forge.test.event-test
    'cn.academy.forge.test.datagen-test
    'cn.academy.forge.test.render-test))

(defn -main [& args]
  (let [results (run-all-tests)]
    ;; Exit with failure if any tests failed
    (System/exit (if (zero? (+ (:fail results) 
                              (:error results)))
                  0 1))))