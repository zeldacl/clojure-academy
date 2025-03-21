(ns cn.academy.block.resource.core
  (:require [mcmod.protocols :refer [IResourceHandler IEnergyStorage]]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Resource state tracking
(def resource-state
  (atom {:limits {}
         :transfer-rates {}}))

;; Basic resource handling implementation
(defrecord ResourceContainer [id state-atom]
  IResourceHandler
  (has-resources? [_ resources]
    (every? (fn [[type amount]]
              (>= (get-in @state-atom [:resources type] 0) amount))
            resources))
  
  (add-resources! [_ resources]
    (error/with-error-handling id :resource
      (doseq [[type amount] resources]
        (let [current (get-in @state-atom [:resources type] 0)
              limit (get-in @resource-state [:limits id type])
              new-amount (+ current amount)]
          (when (and limit (> new-amount limit))
            (throw (ex-info "Resource limit exceeded" 
                          {:type type :amount new-amount :limit limit})))
          (swap! state-atom assoc-in [:resources type] new-amount)))))
  
  (consume-resources! [_ resources]
    (error/with-error-handling id :resource
      (when (has-resources? resources)
        (doseq [[type amount] resources]
          (swap! state-atom update-in [:resources type] #(- (or % 0) amount)))
        true))))

;; Energy storage implementation
(extend-type ResourceContainer
  IEnergyStorage
  (get-energy-stored [this]
    (get-in @(:state-atom this) [:resources :energy] 0))
  
  (get-max-energy-stored [this]
    (get-in @resource-state [:limits (:id this) :energy]))
  
  (receive-energy [this amount simulate]
    (let [current (get-energy-stored this)
          limit (get-max-energy-stored this)
          max-receive (- (or limit Double/MAX_VALUE) current)
          actual-receive (min amount max-receive)]
      (when-not simulate
        (add-resources! this {:energy actual-receive}))
      actual-receive))
  
  (extract-energy [this amount simulate]
    (let [current (get-energy-stored this)
          actual-extract (min amount current)]
      (when-not simulate
        (consume-resources! this {:energy actual-extract}))
      actual-extract)))

;; Resource container creation
(defn create-container!
  "Create a new resource container"
  [id & {:keys [limits]}]
  (when limits
    (swap! resource-state assoc-in [:limits id] limits))
  (->ResourceContainer id (atom {:resources {}})))

;; Resource transfer rate management
(defn set-transfer-rate!
  "Set resource transfer rate for container"
  [id resource-type rate]
  (swap! resource-state assoc-in [:transfer-rates id resource-type] rate))

(defn get-transfer-rate
  "Get resource transfer rate for container"
  [id resource-type]
  (get-in @resource-state [:transfer-rates id resource-type]))

;; Resource balancing 
(defn balance-resources!
  "Balance resources between containers"
  [containers resource-type]
  (let [total (reduce + (map #(get-in @(:state-atom %) [:resources resource-type] 0) containers))
        target (/ total (count containers))]
    (doseq [container containers]
      (let [current (get-in @(:state-atom container) [:resources resource-type] 0)
            diff (- target current)]
        (when (pos? diff)
          (add-resources! container {resource-type diff}))))))