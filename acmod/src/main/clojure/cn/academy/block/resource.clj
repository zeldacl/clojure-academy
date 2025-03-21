(ns cn.academy.block.resource
  (:require [cn.academy.block.error :as error]
            [cn.academy.block.stats :as stats]
            [cn.academy.block.jobs :as jobs]
            [cn.academy.block.network :as network]
            [clojure.tools.logging :as log]))

;; Resource tracking
(def resource-state
  (atom {:limits {}
         :reservations {}
         :transfer-rates {}}))

;; Resource protocols
(defprotocol IResource
  (allocate! [this amount])
  (release! [this amount])
  (transfer! [this target amount])
  (can-allocate? [this amount])
  (get-available [this]))

;; Resource implementation
(defrecord BlockResource [block resource-type]
  IResource
  (allocate! [_ amount]
    (error/with-safe-execution (:id block) :resource
      (when (can-allocate? this amount)
        (swap! (:state block) update-in [:resources resource-type]
               #(- % amount))
        (network/optimize-update! block :resources 
                                (get-in @(:state block) [:resources]))
        true)))
  
  (release! [_ amount]
    (error/with-safe-execution (:id block) :resource
      (let [limit (get-in @resource-state [:limits (:id block) resource-type])]
        (when (or (nil? limit)
                  (<= (+ (get-in @(:state block) [:resources resource-type] 0) 
                        amount)
                      limit))
          (swap! (:state block) update-in [:resources resource-type]
                 #(+ (or % 0) amount))
          (network/optimize-update! block :resources 
                                  (get-in @(:state block) [:resources]))
          true))))
  
  (transfer! [this target amount]
    (error/with-safe-execution (:id block) :resource
      (when (and (can-allocate? this amount)
                 (can-receive? target amount))
        (jobs/create-job! block :resource-transfer
                         :params {:target target
                                 :resource resource-type
                                 :amount amount}))))
  
  (can-allocate? [_ amount]
    (let [current (get-in @(:state block) [:resources resource-type] 0)
          reserved (get-in @resource-state 
                          [:reservations (:id block) resource-type] 
                          0)]
      (>= (- current reserved) amount)))
  
  (get-available [_]
    (let [current (get-in @(:state block) [:resources resource-type] 0)
          reserved (get-in @resource-state 
                          [:reservations (:id block) resource-type] 
                          0)]
      (- current reserved))))

;; Resource management
(defn create-resource! [block resource-type]
  (->BlockResource block resource-type))

(defn set-resource-limit! [block resource-type limit]
  (swap! resource-state assoc-in [:limits (:id block) resource-type] limit))

(defn set-transfer-rate! [block resource-type rate]
  (swap! resource-state assoc-in [:transfer-rates (:id block) resource-type] rate))

;; Resource reservation
(defn reserve-resource! [block resource-type amount]
  (let [resource (create-resource! block resource-type)]
    (when (can-allocate? resource amount)
      (swap! resource-state update-in 
             [:reservations (:id block) resource-type]
             #(+ (or % 0) amount))
      true)))

(defn release-reservation! [block resource-type amount]
  (swap! resource-state update-in 
         [:reservations (:id block) resource-type]
         #(max 0 (- (or % 0) amount))))

;; Resource transfer validation
(defn- can-receive? [block amount]
  (let [limit (get-in @resource-state [:limits (:id block)])]
    (or (nil? limit)
        (<= (+ (get-in @(:state block) [:resources] 0) amount)
            limit))))

;; Resource balancing
(defn balance-resources! [blocks resource-type]
  (let [resources (map #(create-resource! % resource-type) blocks)
        total-available (reduce + (map get-available resources))
        target-amount (/ total-available (count blocks))]
    (doseq [resource resources
            :let [available (get-available resource)
                  diff (- available target-amount)]
            :when (pos? diff)]
      (doseq [target resources
              :when (and (not= (:id (:block resource)) 
                              (:id (:block target)))
                        (< (get-available target) target-amount))]
        (transfer! resource target (min diff (- target-amount 
                                              (get-available target))))))))

;; Resource monitoring
(defn monitor-resources! []
  (doseq [[block-id limits] (:limits @resource-state)
          [resource-type limit] limits
          :let [block (mcmod.block/get-block-by-id block-id)
                current (get-in @(:state block) [:resources resource-type] 0)]
          :when (> current limit)]
    (let [excess (- current limit)]
      (log/warn "Resource limit exceeded:" block-id resource-type)
      (stats/track-resource-excess! block resource-type excess))))

;; Initialize resource system
(defn init-resources! []
  (mcmod.scheduler/schedule-periodic 100
    monitor-resources!))