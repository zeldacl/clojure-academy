(ns cn.academy.block.error
  (:require [cn.academy.block.stats :as stats]
            [clojure.tools.logging :as log]))

;; Error state management
(def error-state
  (atom {:errors {}
         :recovery-strategies {}
         :error-counts {}}))

;; Error types
(def error-categories
  #{:resource
    :network
    :job
    :lifecycle
    :stats
    :validation})

;; Recovery strategies
(defmulti recover-from-error
  (fn [category _block _error] category))

(defmethod recover-from-error :resource [_ block error]
  (try
    (case (:type error)
      :limit-exceeded (do
                       (log/warn "Attempting to release excess resources for block" (:id block))
                       (swap! (:state block) update-in [:resources (:resource error)] #(min % (:limit error))))
      :transfer-failed (do
                        (log/warn "Resource transfer failed for block" (:id block))
                        (stats/track-resource-usage! block (:resource error) (:amount error)))
      ;; Default recovery
      (log/error "No specific recovery strategy for resource error type" (:type error)))
    (catch Exception e
      (log/error "Recovery failed:" (.getMessage e)))))

(defmethod recover-from-error :network [_ block error]
  (try
    (case (:type error)
      :sync-failed (do
                    (log/warn "Network sync failed for block" (:id block))
                    (swap! (:state block) assoc :needs-sync true))
      :connection-lost (do
                        (log/warn "Connection lost for block" (:id block))
                        (swap! (:state block) assoc :connection-state :disconnected))
      ;; Default recovery
      (log/error "No specific recovery strategy for network error type" (:type error)))
    (catch Exception e
      (log/error "Recovery failed:" (.getMessage e)))))

(defmethod recover-from-error :job [_ block error]
  (try
    (case (:type error)
      :execution-failed (do
                         (log/warn "Job execution failed for block" (:id block))
                         (swap! (:state block) update :failed-jobs conj (:job-id error)))
      :timeout (do
                (log/warn "Job timed out for block" (:id block))
                (swap! (:state block) update :timeout-jobs conj (:job-id error)))
      ;; Default recovery
      (log/error "No specific recovery strategy for job error type" (:type error)))
    (catch Exception e
      (log/error "Recovery failed:" (.getMessage e)))))

(defmethod recover-from-error :default [category block error]
  (log/error "No recovery strategy defined for category" category)
  (stats/track-block-suspension! block))

;; Error handling macro
(defmacro with-safe-execution
  [block-id category & body]
  `(try
     ~@body
     (catch Exception e#
       (handle-error! ~block-id ~category e#)
       nil)))

;; Error handling
(defn handle-error! [block-id category ^Exception error]
  (let [error-data {:timestamp (System/currentTimeMillis)
                    :category category
                    :message (.getMessage error)
                    :stacktrace (with-out-str 
                                 (.printStackTrace error (java.io.PrintWriter. *out*)))}]
    (swap! error-state update-in [:errors block-id] 
           #(take 100 (conj (or % []) error-data)))
    (swap! error-state update-in [:error-counts block-id category] 
           (fnil inc 0))
    
    (when-let [block (mcmod.block/get-block-by-id block-id)]
      (recover-from-error category block error-data))))

;; Error querying
(defn get-block-errors [block-id]
  (get-in @error-state [:errors block-id]))

(defn get-error-count [block-id category]
  (get-in @error-state [:error-counts block-id category] 0))

(defn get-total-errors [block-id]
  (reduce + (vals (get-in @error-state [:error-counts block-id] {}))))

;; Error rate monitoring
(def ^:private error-rate-window (* 5 60 1000)) ; 5 minutes

(defn get-error-rate [block-id category]
  (let [errors (get-in @error-state [:errors block-id])
        now (System/currentTimeMillis)
        recent-errors (filter #(>= (:timestamp %) (- now error-rate-window))
                            errors)]
    (/ (count (filter #(= (:category %) category) recent-errors))
       (/ error-rate-window 1000.0))))

;; Custom recovery strategy registration
(defn register-recovery-strategy! [category strategy-fn]
  (swap! error-state assoc-in [:recovery-strategies category] strategy-fn))

;; Error cleanup
(defn cleanup-old-errors! []
  (let [now (System/currentTimeMillis)
        cutoff (- now (* 24 60 60 1000))] ; 24 hours
    (swap! error-state update :errors
           (fn [errors]
             (->> errors
                  (map (fn [[block-id block-errors]]
                        [block-id (filter #(>= (:timestamp %) cutoff)
                                        block-errors)]))
                  (into {}))))))

;; Initialize error handling system
(defn init-error-handling! []
  ;; Cleanup old errors periodically
  (mcmod.scheduler/schedule-periodic
    (* 60 1000) ; Every minute
    cleanup-old-errors!))