(ns cn.academy.forge-test.test-utils
  (:require [clojure.test :refer :all]
            [cn.academy.core.util.logging :refer [with-logging]]
            [cn.academy.core.util.async :as async])
  (:import [java.util.concurrent CompletableFuture TimeUnit]))

(defmacro with-test-timeout [timeout-ms & body]
  `(let [future# (CompletableFuture/supplyAsync
                  (reify java.util.function.Supplier
                    (get [_]
                      ~@body)))]
     (.get future# ~timeout-ms TimeUnit/MILLISECONDS)))

(defmacro with-test-context [description & body]
  `(with-logging ~description
     (testing ~description
       ~@body)))

(defn run-test-async [test-fn]
  (async/submit-task
    #(try
       (test-fn)
       (catch Throwable t
         (println "Async test failed:" (.getMessage t))
         (throw t)))))

(defn await-condition [pred timeout-ms]
  (let [start (System/currentTimeMillis)]
    (while (and (not (pred))
                (< (- (System/currentTimeMillis) start) timeout-ms))
      (Thread/sleep 100))
    (is (pred) "Condition not met within timeout")))

(defmacro with-mock-world [world-sym & body]
  `(let [~world-sym (reify net.minecraft.world.World
                      (dimension [_] 0)
                      (isRemote [_] false)
                      (setTileEntity [_ pos te#] true)
                      (getTileEntity [_ pos#] nil))]
     ~@body))

(defn create-test-config []
  {:cat-engine
   {:energy-gen-rate 10.0
    :wireless-range 32
    :allow-interdimensional true
    :max-energy 100000.0}})

(defn run-with-test-config [f]
  (let [old-config (atom nil)]
    (try
      (reset! old-config @#'cn.academy.core.config/config-state)
      (cn.academy.core.config/load-config! (create-test-config))
      (f)
      (finally
        (reset! #'cn.academy.core.config/config-state @old-config)))))