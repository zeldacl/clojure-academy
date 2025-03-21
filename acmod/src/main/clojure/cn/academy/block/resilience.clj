(ns cn.academy.block.resilience
  (:require [cn.academy.block.error :as error]
            [cn.academy.block.persistence :as persist]
            [cn.academy.block.stats :as stats]
            [cn.academy.block.validation :as validation]
            [clojure.tools.logging :as log]))

;; Failure detection
(def failure-thresholds
  {:error-rate 0.1  ; 10% errors
   :corruption-risk 0.05  ; 5% data corruption risk
   :recovery-attempts 3})

;; Block state snapshot
(defprotocol IStateSnapshot
  (capture-state [this block])
  (restore-state [this block])
  (validate-state [this]))

;; Snapshot implementation
(defrecord BlockSnapshot [block-id timestamp state config]
  IStateSnapshot
  (capture-state [_ block]
    (assoc this
           :state @(:state block)
           :config (:config block)))
  
  (restore-state [_ block]
    (reset! (:state block) state)
    (swap! block assoc :config config))
  
  (validate-state [_]
    (validation/validate-state! state validation/machine-state)))

;; Failure tracking
(def resilience-state
  (atom {:snapshots {}
         :failure-counts {}
         :recovery-history {}}))

;; Snapshot management
(defn create-snapshot! [block]
  (let [snapshot (-> (->BlockSnapshot (:id block) 
                                     (System/currentTimeMillis)
                                     nil nil)
                     (capture-state block))]
    (swap! resilience-state assoc-in 
           [:snapshots (:id block)]
           snapshot)
    snapshot))

;; Failure detection
(defn check-block-health [block]
  (let [block-id (:id block)
        error-stats (stats/get-block-stats block-id :errors)
        total-ops (stats/get-block-stats block-id :operations)
        error-rate (if (pos? (:total total-ops))
                    (/ (:total error-stats) (:total total-ops))
                    0)]
    {:healthy? (< error-rate (:error-rate failure-thresholds))
     :error-rate error-rate
     :corruption-risk (get-in @resilience-state 
                             [:failure-counts block-id :corruption]
                             0)}))

;; Recovery procedures
(defn attempt-recovery! [block]
  (error/with-safe-execution (:id block) :recovery
    (let [block-id (:id block)
          attempts (get-in @resilience-state [:failure-counts block-id :attempts] 0)]
      (when (< attempts (:recovery-attempts failure-thresholds))
        ;; Try restoring from snapshot
        (when-let [snapshot (get-in @resilience-state [:snapshots block-id])]
          (when (validate-state snapshot)
            (restore-state snapshot block)
            (swap! resilience-state update-in 
                   [:recovery-history block-id]
                   conj
                   {:timestamp (System/currentTimeMillis)
                    :successful true})
            true))))))

;; Corruption handling
(defn handle-corruption! [block]
  (error/with-safe-execution (:id block) :corruption
    (let [block-id (:id block)]
      ;; Try loading from persistent storage
      (when-let [nbt (mcmod.block/read-nbt block)]
        (persist/load-block-data! block nbt)
        (create-snapshot! block)  ; Create new snapshot after recovery
        (swap! resilience-state update-in 
               [:failure-counts block-id :corruption]
               (fnil inc 0))
        true))))

;; Health monitoring
(defn monitor-block-health! [block]
  (let [health (check-block-health block)]
    (when-not (:healthy? health)
      (if (> (:corruption-risk health) 
             (:corruption-risk failure-thresholds))
        (handle-corruption! block)
        (attempt-recovery! block)))))

;; Initialize resilience system
(defn init-resilience! []
  (mcmod.scheduler/schedule-periodic 60
    (fn []
      (doseq [block (mcmod.block/get-loaded-blocks)]
        (monitor-block-health! block)))))