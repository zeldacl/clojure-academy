(ns cn.academy.block.world.monitor
  (:require [cn.academy.core.monitoring :as monitoring]
            [cn.academy.core.profiling :as profiling]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; World monitoring state
(def world-state
  (atom {:chunk-stats {}
         :block-updates {}}))

;; Chunk tracking
(defn track-chunk-load!
  "Track chunk load event"
  [chunk-pos load-time]
  (profiling/with-profile "world" "chunk-load"
    (monitoring/with-metrics "world" "chunk-load-time" load-time)
    (swap! world-state update-in [:chunk-stats chunk-pos]
           (fnil update {:loads 0 :total-time 0} :loads inc))
    (swap! world-state update-in [:chunk-stats chunk-pos :total-time]
           (fnil + 0) load-time)))

(defn track-block-update!
  "Track block update event"
  [pos block-type]
  (profiling/with-profile "world" "block-update"
    (monitoring/with-metrics "world" "block-updates" 1)
    (swap! world-state update-in [:block-updates block-type]
           (fnil inc 0))))

;; Performance analysis
(defn analyze-chunk-hotspots
  "Find chunks with slow load times"
  []
  (->> (:chunk-stats @world-state)
       (map (fn [[pos stats]]
              {:pos pos
               :avg-load-time (/ (:total-time stats)
                                (:loads stats))
               :load-count (:loads stats)}))
       (filter #(> (:avg-load-time %) 100))
       (sort-by :avg-load-time >)))

(defn analyze-block-updates
  "Analyze block update patterns"
  []
  (let [updates (:block-updates @world-state)]
    {:total (reduce + (vals updates))
     :by-type (into {} 
                    (for [[type count] updates]
                      [type {:count count
                            :percentage (* 100.0 
                                         (/ count (reduce + (vals updates))))}]))}))

;; Health checks
(defn check-world-health
  "Check world performance health"
  []
  (let [chunk-stats (analyze-chunk-hotspots)
        update-stats (analyze-block-updates)]
    {:chunks {:hot-chunks (take 10 chunk-stats)
             :total-chunks (count (:chunk-stats @world-state))}
     :updates update-stats}))

;; Alert generation
(defn detect-world-issues!
  "Check for world performance issues"
  []
  (let [health (check-world-health)]
    ;; Check for slow chunk loading
    (when-let [worst-chunk (first (get-in health [:chunks :hot-chunks]))]
      (when (> (:avg-load-time worst-chunk) 500)
        (error/set-error! :world
          {:type :chunk-performance
           :message (format "Slow chunk loading at %s: %.2fms avg"
                          (:pos worst-chunk)
                          (:avg-load-time worst-chunk))})))
    
    ;; Check for excessive block updates
    (let [total-updates (get-in health [:updates :total])]
      (when (> total-updates 10000)
        (error/set-error! :world
          {:type :block-updates
           :message (format "Excessive block updates: %d" 
                          total-updates)})))))

;; Statistics reset
(defn reset-statistics! []
  (reset! world-state {:chunk-stats {}
                       :block-updates {}}))

;; Reporting
(defn generate-world-report []
  (let [health (check-world-health)]
    (with-out-str
      (println "=== World Performance Report ===")
      (println)
      (println "Chunk Statistics:")
      (doseq [chunk (get-in health [:chunks :hot-chunks])]
        (println (format "  %s: %.2fms avg (%d loads)"
                        (:pos chunk)
                        (:avg-load-time chunk)
                        (:load-count chunk))))
      (println)
      (println "Block Update Statistics:")
      (let [updates (get-in health [:updates :by-type])]
        (doseq [[type stats] updates]
          (println (format "  %s: %d updates (%.1f%%)"
                          type
                          (:count stats)
                          (:percentage stats))))))))

;; Initialize monitoring
(defn init-world-monitor! []
  (reset-statistics!)
  ;; Start periodic health checks
  (future
    (try
      (while true
        (Thread/sleep 10000)
        (detect-world-issues!))
      (catch InterruptedException _))))