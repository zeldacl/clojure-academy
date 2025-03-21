(ns cn.academy.block.error-monitor
  (:require [cn.academy.block.error :as error]
            [cn.academy.block.stats :as stats]
            [clojure.tools.logging :as log]))

(def monitor-state
  (atom {:thresholds {}
         :actions {}}))

;; Error rate thresholds
(def default-thresholds
  {:resource 0.1      ; errors per second
   :network 0.05
   :job 0.2
   :lifecycle 0.01
   :stats 0.5
   :validation 0.1})

;; Monitor configuration
(defn set-error-threshold! [category threshold]
  (swap! monitor-state assoc-in [:thresholds category] threshold))

(defn register-threshold-action! [category threshold-type action-fn]
  (swap! monitor-state assoc-in [:actions category threshold-type] action-fn))

;; Threshold checking
(defn- check-threshold [block-id category]
  (let [rate (error/get-error-rate block-id category)
        threshold (get-in @monitor-state [:thresholds category]
                         (get default-thresholds category))]
    (when (> rate threshold)
      {:type :threshold-exceeded
       :category category
       :rate rate
       :threshold threshold})))

;; Recovery actions
(defn- execute-recovery-action! [block-id {:keys [category type] :as violation}]
  (when-let [action (get-in @monitor-state [:actions category type])]
    (try
      (action block-id violation)
      (catch Exception e
        (log/error "Recovery action failed for" block-id ":" (.getMessage e))))))

;; Monitoring cycle
(defn check-block! [block-id]
  (doseq [category error/error-categories]
    (when-let [violation (check-threshold block-id category)]
      (log/warn "Error threshold exceeded for block" block-id "category" category)
      (execute-recovery-action! block-id violation)
      (stats/track-threshold-violation! block-id category))))

;; Default recovery actions
(defn- init-default-actions! []
  (register-threshold-action! :resource :threshold-exceeded
    (fn [block-id violation]
      (log/warn "Initiating resource cleanup for block" block-id)
      (error/with-safe-execution block-id :resource
        (mcmod.block/cleanup-resources! block-id))))
  
  (register-threshold-action! :network :threshold-exceeded
    (fn [block-id violation]
      (log/warn "Initiating network reconnection for block" block-id)
      (error/with-safe-execution block-id :network
        (mcmod.block/reconnect! block-id))))
  
  (register-threshold-action! :job :threshold-exceeded
    (fn [block-id violation]
      (log/warn "Cancelling pending jobs for block" block-id)
      (error/with-safe-execution block-id :job
        (mcmod.block/cancel-pending-jobs! block-id)))))

;; System initialization
(defn init-monitor! []
  (init-default-actions!)
  (mcmod.scheduler/schedule-periodic
    (* 10 1000) ; Every 10 seconds
    (fn []
      (doseq [block-id (mcmod.block/get-active-blocks)]
        (check-block! block-id)))))