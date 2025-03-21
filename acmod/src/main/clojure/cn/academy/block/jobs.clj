(ns cn.academy.block.jobs
  (:require [cn.academy.block.error :as error]
            [cn.academy.block.stats :as stats]
            [cn.academy.block.lifecycle :as lifecycle]
            [cn.academy.block.network :as network]
            [clojure.tools.logging :as log]
            [clojure.core.async :as async]))

;; Job state management
(def job-state
  (atom {:pending {}
         :running {}
         :completed {}
         :dependencies {}}))

;; Job priority levels
(def priority-levels
  {:high 0
   :normal 1
   :low 2
   :background 3})

;; Job protocols
(defprotocol IJob
  (start! [this])
  (pause! [this])
  (resume! [this])
  (cancel! [this])
  (get-progress [this]))

;; Job implementation
(defrecord BlockJob [id block type priority params dependencies]
  IJob
  (start! [this]
    (error/with-safe-execution id :job
      (when (every? #(get-in @job-state [:completed %]) dependencies)
        (swap! job-state update :running assoc id this)
        (async/go
          (try
            (let [result ((get job-handlers type) block params)]
              (swap! job-state 
                     (fn [state]
                       (-> state
                           (update :running dissoc id)
                           (assoc-in [:completed id] result)))))
            (catch Exception e
              (log/error "Job failed:" id (.getMessage e))
              (swap! job-state update :running dissoc id)))))))
  
  (pause! [_]
    (when-let [handler (get job-pause-handlers type)]
      (handler block params)))
  
  (resume! [this]
    (start! this))
  
  (cancel! [_]
    (swap! job-state 
           (fn [state]
             (-> state
                 (update :pending dissoc id)
                 (update :running dissoc id)))))
  
  (get-progress [_]
    (when-let [progress-fn (get job-progress-handlers type)]
      (progress-fn block params))))

;; Job creation
(defn create-job! [block type & {:keys [priority params dependencies]
                                :or {priority :normal
                                    dependencies #{}}}]
  (let [job-id (str (random-uuid))
        job (->BlockJob job-id block type priority params dependencies)]
    (swap! job-state 
           (fn [state]
             (-> state
                 (assoc-in [:pending job-id] job)
                 (update :dependencies merge 
                         (zipmap dependencies (repeat #{job-id}))))))
    job-id))

;; Job handlers
(def job-handlers
  {:process-recipe (fn [block params]
                     (let [{:keys [recipe inputs outputs]} params]
                       (when (and (lifecycle/is-active? block)
                                (every? #(>= (get-in @(:state block) [:resources %])
                                           (get-in recipe [:inputs %]))
                                      inputs))
                         (doseq [[resource amount] inputs]
                           (swap! (:state block) update-in [:resources resource] - amount))
                         (doseq [[resource amount] outputs]
                           (swap! (:state block) update-in [:resources resource] + amount))
                         true)))
   
   :energy-transfer (fn [block params]
                     (let [{:keys [target amount]} params]
                       (when-let [energy (get-in @(:state block) [:energy])]
                         (when (>= energy amount)
                           (swap! (:state block) update :energy - amount)
                           (swap! (:state target) update :energy + amount)
                           (network/optimize-update! block :energy 
                                                   (get-in @(:state block) [:energy]))
                           true))))
   
   :inventory-sync (fn [block params]
                    (let [{:keys [target slots]} params]
                      (doseq [slot slots]
                        (let [item (get-in @(:state block) [:inventory slot])]
                          (swap! (:state target) assoc-in [:inventory slot] item)))
                      (network/optimize-update! block :inventory
                                              (get-in @(:state block) [:inventory]))
                      true))})

;; Job progress tracking
(def job-progress-handlers
  {:process-recipe (fn [block params]
                    (let [start-time (get-in @job-state [:running-times (:id block)])]
                      (when start-time
                        (let [elapsed (- (System/currentTimeMillis) start-time)
                              duration (get-in params [:recipe :duration] 1000)]
                          (min 1.0 (/ elapsed duration))))))
   
   :energy-transfer (fn [block params]
                     (let [initial (get params :initial-energy)
                           target (get params :amount)]
                       (when (and initial target)
                         (let [current (- initial (get-in @(:state block) [:energy]))]
                           (min 1.0 (/ current target))))))})

;; Job scheduling
(defn schedule-jobs! []
  (let [pending-jobs (->> (:pending @job-state)
                         (sort-by #(get priority-levels (:priority (val %)) 999))
                         (filter (fn [[_ job]]
                                 (every? #(get-in @job-state [:completed %])
                                        (:dependencies job)))))]
    (doseq [[job-id job] pending-jobs]
      (start! job)
      (swap! job-state update :pending dissoc job-id))))

;; Job cleanup
(defn cleanup-completed-jobs! []
  (let [completed (:completed @job-state)
        dependent-jobs (->> (:dependencies @job-state)
                          vals
                          (apply concat)
                          set)]
    (doseq [job-id (keys completed)]
      (when-not (dependent-jobs job-id)
        (swap! job-state update :completed dissoc job-id)))))

;; Initialize job system
(defn init-jobs! []
  (let [scheduler-interval (/ 1000 20)] ; 20 TPS
    (mcmod.scheduler/schedule-periodic
      scheduler-interval
      (fn []
        (schedule-jobs!)
        (cleanup-completed-jobs!)))))