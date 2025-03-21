(ns cn.academy.block.sync
  (:require [cn.academy.block.network.core :as network]
            [cn.academy.block.error :as error]
            [cn.academy.block.validation :as validation]
            [clojure.tools.logging :as log]))

;; Sync state tracking
(def sync-state (atom {:pending-updates {}
                      :sync-interval 20
                      :last-sync {}}))

;; State diff calculation
(defn calculate-state-diff [old-state new-state]
  (into {}
        (filter (fn [[k v]]
                  (not= v (get old-state k)))
                new-state)))

;; State synchronization
(defn schedule-sync! [block]
  (let [block-id (:id block)
        current-state @(:state block)
        last-sync (get-in @sync-state [:last-sync block-id])]
    (when-let [diff (calculate-state-diff last-sync current-state)]
      (swap! sync-state assoc-in [:pending-updates block-id] diff))))

;; Sync processing
(defn process-pending-syncs! []
  (let [updates (:pending-updates @sync-state)]
    (doseq [[block-id diff] updates]
      (when-let [block (mcmod.block/get-block-by-id block-id)]
        (error/with-safe-execution block-id :sync
          (network/send-message! :sync-state block diff)
          (swap! sync-state 
                 (fn [state]
                   (-> state
                       (update :pending-updates dissoc block-id)
                       (assoc-in [:last-sync block-id] @(:state block))))))))))

;; Client-side state update handling
(defn apply-state-update! [block diff]
  (error/with-safe-execution (:id block) :sync
    (let [current-state @(:state block)
          new-state (merge current-state diff)]
      (when (validation/validate-state! block new-state)
        (reset! (:state block) new-state)
        true))))

;; Initialize sync system
(defn init-sync-system! []
  (network/register-handler! :sync-state
    (fn [msg]
      (when-let [block (mcmod.block/get-block-by-id (:block-id msg))]
        (apply-state-update! block (:diff msg)))))
  
  ;; Start periodic sync processing
  (mcmod.scheduler/schedule-periodic
    (:sync-interval @sync-state)
    process-pending-syncs!))