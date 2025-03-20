(ns cn.academy.capability
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core]
            [clojure.tools.logging :as log]))

(defrecord EnergyStorage [current-energy max-energy]
  IEnergyStorage
  (receive-energy [this max-receive simulate]
    (let [space (- max-energy @current-energy)
          actual (min max-receive space)]
      (when-not simulate
        (swap! current-energy + actual))
      actual))
      
  (extract-energy [this max-extract simulate]
    (let [actual (min max-extract @current-energy)]
      (when-not simulate
        (swap! current-energy - actual))
      actual))
      
  (get-energy-stored [this]
    @current-energy)
    
  (get-max-energy-stored [this]
    max-energy)
    
  (can-extract? [this] true)
  (can-receive? [this] true))

(defrecord ItemHandler [inventory slot-count]
  IItemHandler
  (get-slots [this]
    slot-count)
    
  (get-stack-in-slot [this slot]
    (when (< slot slot-count)
      (get-stack inventory slot)))
      
  (insert-item [this slot stack simulate]
    (when (and (< slot slot-count)
               (is-valid-stack? inventory slot stack))
      (let [existing (get-stack inventory slot)
            max-size (get-max-stack-size inventory)
            can-insert (if existing
                        (- max-size (get-count existing))
                        max-size)
            actual (min can-insert (get-count stack))]
        (when-not simulate
          (if existing
            (set-count existing (+ (get-count existing) actual))
            (set-stack inventory slot (assoc stack :count actual))))
        (update stack :count - actual))))
        
  (extract-item [this slot amount simulate]
    (when (< slot slot-count)
      (when-let [existing (get-stack inventory slot)]
        (let [actual (min amount (get-count existing))]
          (when-not simulate
            (if (= actual (get-count existing))
              (set-stack inventory slot nil)
              (set-count existing (- (get-count existing) actual))))
          (assoc existing :count actual)))))
          
  (get-slot-limit [this slot]
    (when (< slot slot-count)
      (get-max-stack-size inventory)))
      
  (is-item-valid? [this slot stack]
    (and (< slot slot-count)
         (is-valid-stack? inventory slot stack))))

(defrecord CapabilityProvider [capabilities]  
  ICapabilityProvider
  (get-capability [this cap side]
    (get capabilities cap))
    
  (has-capability? [this cap side]
    (contains? capabilities cap))
    
  (invalidate-capabilities [this]
    (reset! capabilities {})))

(defn create-energy-storage [max-energy]
  (->EnergyStorage (atom 0) max-energy))
  
(defn create-item-handler [inventory slots]
  (->ItemHandler inventory slots))
  
(defn create-capability-provider [& caps]
  (->CapabilityProvider (atom (into {} caps))))