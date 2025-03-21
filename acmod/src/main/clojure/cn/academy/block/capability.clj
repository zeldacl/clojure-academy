(ns cn.academy.block.capability
  (:require [mcmod.protocols :refer [ICapabilityProvider ICapabilityType]]
            [clojure.tools.logging :as log]))

;; Capability types
(defprotocol ICapabilityFactory
  (create-capability [this block side]))

;; Capability state tracking  
(def capability-state
  (atom {:registered-capabilities {}
         :capability-cache {}}))

;; Implementation of capability provider
(defrecord BlockCapabilityProvider [block]
  ICapabilityProvider
  (has-capability? [_ cap side]
    (when-let [factory (get-in @capability-state [:registered-capabilities cap])]
      (create-capability factory block side)))
  
  (get-capability [this cap side]
    (when (.has-capability? this cap side)
      (or (get-in @capability-state [:capability-cache [(:id block) cap side]])
          (let [factory (get-in @capability-state [:registered-capabilities cap])
                capability (create-capability factory block side)]
            (swap! capability-state assoc-in 
                   [:capability-cache [(:id block) cap side]]
                   capability)
            capability))))
  
  (invalidate-capabilities [_]
    (swap! capability-state update :capability-cache 
           #(into {} (remove (fn [[[block-id _ _] _]]
                             (= block-id (:id block)))
                           %)))))

;; Default capability types
(defrecord EnergyStorage [block]
  ICapabilityFactory
  (create-capability [_ _ side]
    (when (satisfies? mcmod.protocols/IEnergyStorage block)
      (reify mcmod.protocols/IEnergyStorage
        (get-energy-stored [_]
          (.get-energy-stored block))
        (get-max-energy-stored [_]
          (.get-max-energy-stored block))
        (receive-energy [_ amount simulate]
          (.receive-energy block amount simulate))
        (extract-energy [_ amount simulate]
          (.extract-energy block amount simulate))))))

(defrecord FluidStorage [block]
  ICapabilityFactory
  (create-capability [_ _ side]
    (when (satisfies? mcmod.protocols/IFluidStorage block)
      (reify mcmod.protocols/IFluidStorage
        (get-fluid [_]
          (.get-fluid block))
        (get-capacity [_]
          (.get-capacity block))
        (fill [_ resource simulate]
          (.fill block resource simulate))
        (drain [_ amount simulate]
          (.drain block amount simulate))))))

;; Public interface
(defn register-capability! [capability-type factory]
  (swap! capability-state assoc-in 
         [:registered-capabilities capability-type] 
         factory))

(defn create-capability-provider [block]
  (->BlockCapabilityProvider block))

;; Initialize capability system
(defn init-capabilities! []
  (register-capability! :energy (->EnergyStorage nil))
  (register-capability! :fluid (->FluidStorage nil)))