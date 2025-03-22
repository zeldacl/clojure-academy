(ns cn.academy.tech-system.energy-system.capability.block
  (:require [mcmod.protocols :refer [ICapabilityProvider ICapabilityType IEnergyStorage IFluidStorage IWirelessNode]]
            [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
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

;; Default capability types - Energy Storage
(defrecord EnergyStorage [block]
  ICapabilityFactory
  (create-capability [_ _ side]
    (when (satisfies? IEnergyStorage block)
      (reify IEnergyStorage
        (get-energy-stored [_]
          (.get-energy-stored block))
        (get-max-energy-stored [_]
          (.get-max-energy-stored block))
        (receive-energy [_ amount simulate]
          (.receive-energy block amount simulate))
        (extract-energy [_ amount simulate]
          (.extract-energy block amount simulate))))))

;; Default capability types - Fluid Storage
(defrecord FluidStorage [block]
  ICapabilityFactory
  (create-capability [_ _ side]
    (when (satisfies? IFluidStorage block)
      (reify IFluidStorage
        (get-fluid [_]
          (.get-fluid block))
        (get-capacity [_]
          (.get-capacity block))
        (fill [_ resource simulate]
          (.fill block resource simulate))
        (drain [_ amount simulate]
          (.drain block amount simulate))))))

;; Wireless Network capability
(defrecord WirelessNetworkCapability [node]
  IWirelessNode
  (get-range [this]
    (get-range node))
    
  (get-max-connections [this]
    (get-max-connections node))
    
  (get-max-energy [this]
    (get-max-energy-stored node))
    
  (get-energy [this]
    (get-energy-stored node))
    
  (connect [this other]
    (when (can-connect? node other)
      (connect node other)))
      
  (disconnect [this other]
    (disconnect node other))
    
  (can-connect? [this other]
    (can-connect? node other))

  IEnergyStorage
  (receive-energy [this amount simulate]
    (receive-energy node amount simulate))
    
  (extract-energy [this amount simulate]
    (extract-energy node amount simulate))
    
  (get-energy-stored [this]
    (get-energy-stored node))
    
  (get-max-energy-stored [this]
    (get-max-energy-stored node))
    
  (can-receive? [this]
    true)
    
  (can-extract? [this]
    true))

;; Public interface
(defn register-capability! [capability-type factory]
  (swap! capability-state assoc-in 
         [:registered-capabilities capability-type] 
         factory))

(defn create-capability-provider [block]
  (->BlockCapabilityProvider block))

(defn create-wireless-capability [node]
  (->WirelessNetworkCapability node))

;; Factory functions for creating block capabilities
(defn add-energy-capability [provider max-energy]
  (let [storage (wireless/create-capability max-energy 16.0 8)]
    (assoc provider :energy-storage storage)))

(defn add-inventory-capability [provider inventory slots]
  (assoc provider :item-handler inventory))

(defn add-wireless-capability [provider node]
  (let [wireless (create-wireless-capability node)]
    (assoc provider :wireless wireless)))

;; Initialize capability system
(defn init-capabilities! []
  (register-capability! :energy (->EnergyStorage nil))
  (register-capability! :fluid (->FluidStorage nil)))