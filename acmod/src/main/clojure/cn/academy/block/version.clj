(ns cn.academy.block.version
  (:require [mcmod.protocols :refer [IVersionManager IMigration]]
            [cn.academy.block.error :as error]
            [cn.academy.block.storage :as storage]
            [clojure.tools.logging :as log]))

;; Version state tracking
(def version-state
  (atom {:current-version nil
         :migrations {}
         :applied-migrations #{}}))

;; Migration implementation
(defrecord DataMigration [id version changes]
  IMigration
  (get-id [_] id)
  
  (get-version [_] version)
  
  (apply-changes! [_]
    (error/with-error-handling :migration
      (doseq [change changes]
        (change))
      true))
  
  (rollback! [_]
    false)) ; Migrations are forward-only for now

;; Version manager implementation
(defrecord VersionManager [state-atom]
  IVersionManager
  (register-migration! [_ migration]
    (swap! state-atom assoc-in 
           [:migrations (.get-version migration) (.get-id migration)]
           migration))
  
  (get-pending-migrations [_]
    (let [current (:current-version @state-atom)
          applied (:applied-migrations @state-atom)]
      (->> (:migrations @state-atom)
           vals
           (mapcat vals)
           (remove #(contains? applied (.get-id %)))
           (filter #(> (.get-version %) current))
           (sort-by #(.get-version %)))))
  
  (apply-migration! [_ migration]
    (when (.apply-changes! migration)
      (swap! state-atom update :applied-migrations conj (.get-id migration))
      (swap! state-atom assoc :current-version (.get-version migration))))
  
  (get-current-version [_]
    (:current-version @state-atom)))

;; Factory functions
(defn create-manager []
  (->VersionManager version-state))

(defn create-migration [id version changes]
  (->DataMigration id version changes))

;; Migration definitions
(def migrations
  [{:id "add-energy-stats"
    :version 1.1
    :changes [#(storage/update-all-blocks! 
                (fn [block]
                  (update block :stats merge {:total-energy-consumed 0
                                            :peak-energy-usage 0})))]}
   
   {:id "upgrade-machine-state"
    :version 1.2
    :changes [#(storage/update-all-blocks!
                (fn [block]
                  (-> block
                      (update :state assoc :version 2)
                      (update :upgrades #(mapv (fn [u] 
                                               (assoc u :installed-at 
                                                      (System/currentTimeMillis)))
                                             %)))
                  ))]}
   
   {:id "add-resource-tracking"
    :version 1.3
    :changes [#(storage/update-all-blocks!
                (fn [block]
                  (assoc-in block [:tracking :resources] 
                           {:inputs {}
                            :outputs {}})))]}])

;; Version checking
(defn needs-migration? [manager]
  (not (empty? (.get-pending-migrations manager))))

;; Migration process
(defn migrate! [manager]
  (let [pending (.get-pending-migrations manager)]
    (when (seq pending)
      (log/info "Starting migration process from version" 
                (.get-current-version manager))
      (doseq [migration pending]
        (log/info "Applying migration" (.get-id migration) 
                 "to version" (.get-version migration))
        (.apply-migration! manager migration))
      (log/info "Migration complete. Current version:" 
                (.get-current-version manager)))))

;; Initialize version system
(defn init-version! []
  (reset! version-state {:current-version 1.0
                        :migrations {}
                        :applied-migrations #{}})
  
  (let [manager (create-manager)]
    ;; Register migrations
    (doseq [{:keys [id version changes]} migrations]
      (.register-migration! manager 
                          (create-migration id version changes)))
    
    ;; Run pending migrations
    (when (needs-migration? manager)
      (migrate! manager))))