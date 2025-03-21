(ns cn.academy.block.resource
  (:require [cn.academy.block.error :as error]
            [cn.academy.block.stats :as stats]
            [cn.academy.block.jobs :as jobs]
            [cn.academy.block.network :as network]
            [mcmod.protocols :refer [IEnergyStorage IFluidStorage]]
            [clojure.tools.logging :as log]))

;; Resource tracking
(def resource-state
  (atom {:limits {}
         :reservations {}
         :transfer-rates {}}))

;; Resource implementation
(defrecord BlockResource [block resource-type]
  IEnergyStorage
  (receive-energy [_ amount simulate]
    (error/with-safe-execution (:id block) :resource
      (let [current (get-in @(:state block) [:resources resource-type] 0)
            limit (get-in @resource-state [:limits (:id block) resource-type])
            max-receive (- (or limit Double/MAX_VALUE) current)
            actual-receive (min amount max-receive)]
        (when-not simulate 
          (swap! (:state block) update-in [:resources resource-type] 
                 #(+ (or % 0) actual-receive))
          (network/optimize-update! block :resources 
                                  (get-in @(:state block) [:resources])))
        actual-receive)))

  (extract-energy [_ amount simulate]
    (error/with-safe-execution (:id block) :resource
      (let [current (get-in @(:state block) [:resources resource-type] 0)
            reserved (get-in @resource-state [:reservations (:id block) resource-type] 0)
            available (- current reserved)
            actual-extract (min amount available)]
        (when-not simulate
          (swap! (:state block) update-in [:resources resource-type]
                 #(- (or % 0) actual-extract)))
        actual-extract)))

  (get-energy-stored [_]
    (get-in @(:state block) [:resources resource-type] 0))

  (get-max-energy-stored [_]
    (get-in @resource-state [:limits (:id block) resource-type] Double/MAX_VALUE)))

;; Resource management
(defn create-resource! [block resource-type]
  (->BlockResource block resource-type))

(defn set-resource-limit! [block resource-type limit]
  (swap! resource-state assoc-in [:limits (:id block) resource-type] limit))

(defn set-transfer-rate! [block resource-type rate]
  (swap! resource-state assoc-in [:transfer-rates (:id block) resource-type] rate))

;; Resource balancing
(defn balance-resources! [blocks resource-type]
  (let [resources (map #(create-resource! % resource-type) blocks)
        total-available (reduce + (map #(.get-energy-stored %) resources))
        target-amount (/ total-available (count blocks))]
    (doseq [source resources
            :let [available (.get-energy-stored source)
                  diff (- available target-amount)]
            :when (pos? diff)]
      (doseq [target resources
              :when (and (not= (:id (:block source)) 
                              (:id (:block target)))
                        (< (.get-energy-stored target) target-amount))]
        (let [transfer-amount (min diff (- target-amount 
                                         (.get-energy-stored target)))]
          (.extract-energy source transfer-amount false)
          (.receive-energy target transfer-amount false))))))

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