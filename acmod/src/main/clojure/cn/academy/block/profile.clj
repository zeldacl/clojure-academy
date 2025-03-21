(ns cn.academy.block.profile
  (:require [mcmod.protocols :refer [IProfiler IProfile]]
            [cn.academy.block.error :as error]
            [cn.academy.block.stats :as stats]
            [cn.academy.block.config :as config]
            [clojure.tools.logging :as log]))

;; Profile state tracking
(def profile-state
  (atom {:active-profiles {}
         :profile-data {}}))

;; Profile implementation
(defrecord ExecutionProfile [id category data-atom]
  IProfile
  (start! [_]
    (swap! data-atom assoc :start-time (System/nanoTime)))
  
  (stop! [_]
    (let [end-time (System/nanoTime)
          start-time (:start-time @data-atom)
          duration (/ (- end-time start-time) 1000000.0)] ; Convert to ms
      (swap! data-atom merge
             {:end-time end-time
              :duration duration
              :samples (conj (:samples @data-atom) duration)})))
  
  (add-event! [_ event]
    (swap! data-atom update :events conj 
           (assoc event :timestamp (System/nanoTime))))
  
  (get-stats [_]
    (let [{:keys [samples events]} @data-atom]
      {:min (when (seq samples) (apply min samples))
       :max (when (seq samples) (apply max samples))
       :avg (when (seq samples) (/ (apply + samples) (count samples)))
       :count (count samples)
       :events (count events)})))

;; Profiler implementation
(defrecord BlockProfiler [state-atom]
  IProfiler
  (start-profile! [_ id category]
    (let [profile (->ExecutionProfile id category 
                                     (atom {:samples []
                                           :events []}))]
      (swap! state-atom assoc-in [:active-profiles id] profile)
      (.start! profile)
      profile))
  
  (stop-profile! [_ id]
    (when-let [profile (get-in @state-atom [:active-profiles id])]
      (.stop! profile)
      (swap! state-atom update :profile-data update category 
             (fnil conj []) (.get-stats profile))
      (swap! state-atom update :active-profiles dissoc id)))
  
  (get-profile-data [_ category]
    (get-in @state-atom [:profile-data category]))
  
  (clear-profile-data! [_]
    (swap! state-atom assoc :profile-data {})))

;; Factory functions
(defn create-profiler []
  (->BlockProfiler profile-state))

;; Profiling macros
(defmacro with-profile [id category & body]
  `(let [profiler# (create-profiler)
         profile# (.start-profile! profiler# ~id ~category)]
     (try
       ~@body
       (finally
         (.stop-profile! profiler# ~id)))))

;; Profile reporting
(defn generate-profile-report [category]
  (let [profiler (create-profiler)
        data (.get-profile-data profiler category)]
    {:category category
     :samples (count data)
     :stats (when (seq data)
             {:min (apply min (map :min data))
              :max (apply max (map :max data))
              :avg (/ (apply + (map :avg data))
                     (count data))
              :total-events (apply + (map :events data))})}))

;; Performance thresholds
(def performance-thresholds
  {:machine {:operation 100  ; ms
            :update 50}
   :network {:packet 10
            :connection 1000}
   :world {:chunk-load 500
          :block-update 5}})

;; Performance checking
(defn check-performance! [category operation duration]
  (when-let [threshold (get-in performance-thresholds [category operation])]
    (when (> duration threshold)
      (log/warn "Performance warning:"
                (str (name category) "/" (name operation))
                "took" duration "ms (threshold:" threshold "ms)"))))

;; Initialize profiling system
(defn init-profiler! []
  (reset! profile-state {:active-profiles {}
                        :profile-data {}})
  
  ;; Set up performance monitoring
  (let [profiler (create-profiler)]
    ;; Monitor machine operations
    (stats/track-operation! :machine-operation
                          #(with-profile "machine-op" :machine %))
    
    ;; Monitor network activity  
    (stats/track-operation! :network-packet
                          #(with-profile "network-packet" :network %))
    
    ;; Monitor world updates
    (stats/track-operation! :world-update
                          #(with-profile "world-update" :world %))))