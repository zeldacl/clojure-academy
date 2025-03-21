(ns cn.academy.block.stats
  (:require [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]
            [clojure.data.priority-map :refer [priority-map]]))

;; Stats collection
(def stats-state
  (atom {:metrics {}
         :thresholds {}
         :alerts {}
         :history {}}))

;; Metric types
(def metric-types
  {:counter {:aggregator +
            :init 0}
   :gauge {:aggregator (fn [_ new] new)
           :init 0}
   :histogram {:aggregator conj
              :init []}})

;; Metric definition
(defprotocol IMetric
  (record! [this value])
  (get-value [this])
  (reset! [this]))

(defrecord Metric [block type name]
  IMetric
  (record! [_ value]
    (error/with-safe-execution (:id block) :stats
      (swap! stats-state update-in [:metrics (:id block) name]
             (get-in metric-types [type :aggregator])
             value)
      (swap! stats-state update-in 
             [:history (:id block) name]
             #(take 1000 (conj (or % []) 
                              {:timestamp (System/currentTimeMillis)
                               :value value})))
      (check-thresholds! block name)))
  
  (get-value [_]
    (get-in @stats-state [:metrics (:id block) name]
            (get-in metric-types [type :init])))
  
  (reset! [_]
    (swap! stats-state assoc-in 
           [:metrics (:id block) name]
           (get-in metric-types [type :init]))))

;; Metric creation
(defn create-metric! [block type name]
  (->Metric block type name))

;; Threshold management
(defn set-threshold! [block metric-name {:keys [warning critical] :as levels}]
  (swap! stats-state assoc-in [:thresholds (:id block) metric-name] levels))

(defn- check-thresholds! [block metric-name]
  (when-let [threshold (get-in @stats-state [:thresholds (:id block) metric-name])]
    (let [value (get-in @stats-state [:metrics (:id block) metric-name])
          {:keys [warning critical]} threshold]
      (cond
        (and critical (>= value critical))
        (trigger-alert! block metric-name :critical value)
        
        (and warning (>= value warning))
        (trigger-alert! block metric-name :warning value)))))

;; Alert management
(defn- trigger-alert! [block metric-name level value]
  (let [alert {:timestamp (System/currentTimeMillis)
               :level level
               :value value}]
    (swap! stats-state assoc-in [:alerts (:id block) metric-name] alert)
    (log/warn "Alert triggered:" (:id block) metric-name level value)))

(defn get-active-alerts []
  (:alerts @stats-state))

;; Statistics tracking
(defn track-block-activation! [block]
  (let [metric (create-metric! block :counter :activations)]
    (record! metric 1)))

(defn track-block-suspension! [block]
  (let [metric (create-metric! block :counter :suspensions)]
    (record! metric 1)))

(defn track-resource-usage! [block resource-type amount]
  (let [metric (create-metric! block :histogram (keyword (str (name resource-type) "-usage")))]
    (record! metric amount)))

(defn track-resource-excess! [block resource-type amount]
  (let [metric (create-metric! block :counter (keyword (str (name resource-type) "-excess")))]
    (record! metric amount)))

(defn track-operation-time! [block operation duration]
  (let [metric (create-metric! block :histogram (keyword (str (name operation) "-time")))]
    (record! metric duration)))

;; Statistics aggregation
(defn get-block-stats [block]
  (get-in @stats-state [:metrics (:id block)]))

(defn get-resource-usage-stats [block resource-type window]
  (let [history (get-in @stats-state [:history (:id block) 
                                     (keyword (str (name resource-type) "-usage"))])
        now (System/currentTimeMillis)
        cutoff (- now window)]
    (->> history
         (filter #(>= (:timestamp %) cutoff))
         (map :value))))

;; Performance tracking
(def performance-state
  (atom {:operation-times (priority-map)
         :slow-operations {}}))

(defn track-performance! [block operation duration]
  (swap! performance-state update :operation-times assoc 
         [(:id block) operation] duration)
  (when (> duration 50) ; More than 50ms is considered slow
    (swap! performance-state update-in 
           [:slow-operations (:id block) operation]
           #(conj (or % []) duration))))

(defn get-slow-operations []
  (:slow-operations @performance-state))

;; Initialize statistics system
(defn init-stats! []
  ;; Cleanup old history periodically
  (mcmod.scheduler/schedule-periodic
    (* 60 1000) ; Every minute
    (fn []
      (let [now (System/currentTimeMillis)
            cutoff (- now (* 24 60 60 1000))] ; 24 hours
        (swap! stats-state update :history
               (fn [history]
                 (->> history
                      (map (fn [[block-id metrics]]
                            [block-id
                             (->> metrics
                                  (map (fn [[metric-name values]]
                                        [metric-name
                                         (filter #(>= (:timestamp %) cutoff)
                                                values)]))
                                  (into {}))]))
                      (into {}))))))))