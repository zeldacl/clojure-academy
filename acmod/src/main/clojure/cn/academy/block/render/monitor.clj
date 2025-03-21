(ns cn.academy.block.render.monitor
  (:require [cn.academy.core.monitoring :as monitoring]
            [cn.academy.core.profiling :as profiling]
            [cn.academy.block.error :as error]
            [cn.academy.block.render.core :as render]
            [clojure.tools.logging :as log]))

;; Render monitoring state
(def render-state 
  (atom {:frame-times []
         :batch-stats {}
         :culling-stats {}}))

;; Performance tracking
(defn track-frame-time!
  "Track frame render time"
  [frame-time]
  (profiling/with-profile "render" "frame"
    (monitoring/with-metrics "render" "frame-time" frame-time)
    (swap! render-state update :frame-times conj frame-time)
    (when (> (count (:frame-times @render-state)) 120) ; Keep 2 seconds of history
      (swap! render-state update :frame-times subvec 1))))

(defn track-batch!
  "Track render batch statistics"
  [batch-type vertex-count]
  (profiling/with-profile "render" "batch"
    (monitoring/with-metrics "render" "batch-vertices" vertex-count)
    (swap! render-state update-in [:batch-stats batch-type]
           (fnil update {:count 0 :total-vertices 0} :count inc))
    (swap! render-state update-in [:batch-stats batch-type :total-vertices]
           (fnil + 0) vertex-count)))

(defn track-culling!
  "Track frustum culling results"
  [visible total]
  (profiling/with-profile "render" "culling"
    (let [culled (- total visible)
          ratio (/ visible total)]
      (monitoring/with-metrics "render" "culling-ratio" ratio)
      (swap! render-state update :culling-stats merge
             {:visible visible
              :total total
              :culled culled
              :ratio ratio}))))

;; Performance analysis
(defn analyze-frame-times []
  (let [times (:frame-times @render-state)
        avg (/ (reduce + times) (max 1 (count times)))
        min-time (apply min times)
        max-time (apply max times)]
    {:average avg
     :min min-time
     :max max-time
     :fps (/ 1000.0 avg)}))

(defn analyze-batch-efficiency []
  (let [stats (:batch-stats @render-state)]
    (into {}
          (for [[type data] stats]
            [type {:batches (:count data)
                  :avg-vertices (/ (:total-vertices data)
                                 (max 1 (:count data)))}]))))

;; Performance optimization suggestions
(defn generate-optimization-suggestions []
  (let [frame-analysis (analyze-frame-times)
        batch-analysis (analyze-batch-efficiency)
        culling-stats (:culling-stats @render-state)]
    (with-out-str
      (when (< (:fps frame-analysis) 60)
        (println "- Consider reducing render distance or enabling dynamic culling"))
      
      (doseq [[type stats] batch-analysis]
        (when (> (:batches stats) 1000)
          (println (format "- High batch count for %s: %d batches"
                         type (:batches stats))))
        (when (< (:avg-vertices stats) 100)
          (println (format "- Low vertex density for %s: %.1f vertices/batch"
                         type (:avg-vertices stats)))))
      
      (when (< (:ratio culling-stats) 0.2)
        (println "- Large number of culled objects, consider spatial partitioning optimization")))))

;; Health monitoring
(defn check-render-health []
  (let [frame-stats (analyze-frame-times)]
    (when (< (:fps frame-stats) 30)
      (error/set-error! :render
        {:type :performance
         :message (format "Low FPS: %.1f (%.2fms per frame)"
                        (:fps frame-stats)
                        (:average frame-stats))}))))

;; Reporting
(defn generate-render-report []
  (let [frame-stats (analyze-frame-times)
        batch-stats (analyze-batch-efficiency)]
    (with-out-str
      (println "=== Render Performance Report ===")
      (println)
      (println "Frame Statistics:")
      (println (format "  FPS: %.1f" (:fps frame-stats)))
      (println (format "  Frame Time: %.2fms (%.2f-%.2fms)"
                      (:average frame-stats)
                      (:min frame-stats)
                      (:max frame-stats)))
      (println)
      (println "Batch Statistics:")
      (doseq [[type stats] batch-stats]
        (println (format "  %s:" type))
        (println (format "    Batches: %d" (:batches stats)))
        (println (format "    Avg Vertices: %.1f"
                        (:avg-vertices stats))))
      (println)
      (println "Optimization Suggestions:")
      (println (generate-optimization-suggestions)))))

;; Initialize monitoring
(defn init-render-monitor! []
  (reset! render-state {:frame-times []
                       :batch-stats {}
                       :culling-stats {}})
  ;; Start periodic health checks
  (future
    (try
      (while true
        (Thread/sleep 1000)
        (check-render-health))
      (catch InterruptedException _))))