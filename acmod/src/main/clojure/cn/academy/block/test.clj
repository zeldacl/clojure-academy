(ns cn.academy.block.test
  (:require [mcmod.protocols :refer [ITestRunner ITestCase]]
            [cn.academy.block.error :as error]  
            [cn.academy.block.monitor :as monitor]
            [cn.academy.block.profile :as profile]
            [cn.academy.block.debug :as debug]
            [cn.academy.block.stats :as stats]
            [clojure.tools.logging :as log]))

;; Test state tracking
(def test-state
  (atom {:test-cases {}
         :results {}
         :running nil}))

;; Test case implementation
(defrecord BlockTestCase [id category setup-fn test-fn cleanup-fn]
  ITestCase
  (setup! [_]
    (error/with-error-handling :test
      (setup-fn)))
  
  (run-test! [_]
    (error/with-error-handling :test
      (test-fn)))
  
  (cleanup! [_]
    (error/with-error-handling :test
      (cleanup-fn))))

;; Test runner implementation 
(defrecord BlockTestRunner [state-atom]
  ITestRunner
  (register-test! [_ test-case]
    (swap! state-atom assoc-in [:test-cases (:id test-case)] test-case))
  
  (run-test! [_ id]
    (when-let [test-case (get-in @state-atom [:test-cases id])]
      (swap! state-atom assoc :running id)
      (let [start-time (System/currentTimeMillis)
            result (try
                    (.setup! test-case)
                    (let [test-result (.run-test! test-case)]
                      {:status :pass
                       :result test-result})
                    (catch Exception e
                      {:status :fail
                       :error (.getMessage e)})
                    (finally
                      (.cleanup! test-case)))]
        (swap! state-atom assoc-in [:results id]
               (assoc result
                      :duration (- (System/currentTimeMillis) start-time)
                      :timestamp (System/currentTimeMillis)))
        (swap! state-atom assoc :running nil)
        result)))
  
  (run-category! [this category]
    (->> (:test-cases @state-atom)
         vals
         (filter #(= (:category %) category))
         (map #(.run-test! this (:id %)))
         doall))
  
  (run-all! [this]
    (doseq [test-case (vals (:test-cases @state-atom))]
      (.run-test! this (:id test-case))))
  
  (get-results [_]
    (:results @state-atom))
  
  (clear-results! [_]
    (swap! state-atom assoc :results {})))

;; Factory functions
(defn create-runner []
  (->BlockTestRunner test-state))

(defn create-test [id category setup test cleanup]
  (->BlockTestCase id category setup test cleanup))

;; Test helpers
(defmacro with-test-monitoring [& body]
  `(let [old-errors# (error/get-active-errors)
         old-metrics# (monitor/get-all-metrics)]
     (try
       ~@body
       (finally
         ;; Check for new errors
         (let [new-errors# (error/get-active-errors)
               error-diff# (remove (set old-errors#) new-errors#)]
           (when (seq error-diff#)
             (throw (ex-info "Test produced errors"
                           {:errors error-diff#}))))
         ;; Check metrics changed
         (let [new-metrics# (monitor/get-all-metrics)
               metric-diff# (into {} 
                                (filter (fn [[k v]]
                                        (not= v (get old-metrics# k)))
                                      new-metrics#))]
           (when (seq metric-diff#)
             (log/debug "Metrics changed during test:" metric-diff#)))))))

;; Standard test cases
(def standard-tests
  [{:id "machine-creation"
    :category :machine
    :setup #(do)
    :test #(let [machine (mcmod.machine/create-test-machine)]
            (assert machine "Machine created")
            (assert (mcmod.machine/is-valid? machine) "Machine valid")
            true)
    :cleanup mcmod.machine/cleanup-test-machines!}
   
   {:id "network-connection"
    :category :network
    :setup mcmod.network/setup-test-network!
    :test #(let [net (mcmod.network/get-test-network)]
            (assert net "Network exists")
            (assert (mcmod.network/is-connected? net) "Network connected")
            true)
    :cleanup mcmod.network/cleanup-test-network!}
   
   {:id "fluid-transfer"
    :category :fluid
    :setup mcmod.fluid/setup-test-tanks!
    :test #(let [result (mcmod.fluid/test-fluid-transfer!)]
            (assert result "Fluid transferred")
            true)
    :cleanup mcmod.fluid/cleanup-test-tanks!}])

;; Test reporting
(defn generate-test-report []
  (let [runner (create-runner)
        results (.get-results runner)]
    {:timestamp (System/currentTimeMillis)
     :summary {:total (count results)
              :passed (count (filter #(= :pass (:status %)) (vals results)))
              :failed (count (filter #(= :fail (:status %)) (vals results)))}
     :results (->> results
                  (group-by (comp :category second))
                  (map (fn [[k v]]
                        [k {:total (count v)
                            :passed (count (filter #(= :pass (:status %)) v))
                            :failed (count (filter #(= :fail (:status %)) v))
                            :duration (apply + (map :duration v))}]))
                  (into {}))}))

;; Initialize test system
(defn init-test! []
  (reset! test-state {:test-cases {}
                      :results {}
                      :running nil})
  
  (let [runner (create-runner)]
    ;; Register standard tests
    (doseq [{:keys [id category setup test cleanup]} standard-tests]
      (.register-test! runner 
                      (create-test id category setup test cleanup)))))