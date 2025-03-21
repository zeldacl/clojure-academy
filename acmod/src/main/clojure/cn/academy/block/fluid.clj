(ns cn.academy.block.fluid
  (:require [mcmod.protocols :refer [IFluidHandler IFluidTank IFluidStack]]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Fluid state tracking
(def fluid-state
  (atom {:tanks {}}))

;; Fluid stack implementation
(defrecord FluidStack [fluid amount]
  IFluidStack
  (get-fluid [_] fluid)
  (get-amount [_] amount)
  (split [this amount]
    (let [split-amount (min amount (:amount this))]
      [(assoc this :amount (- (:amount this) split-amount))
       (->FluidStack fluid split-amount)]))
  (grow [this amount]
    (update this :amount + amount))
  (shrink [this amount]
    (update this :amount - amount)))

;; Fluid tank implementation
(defrecord FluidTank [capacity accepts-fluid state-atom]
  IFluidTank 
  (get-fluid-stored [_]
    (:fluid @state-atom))
  
  (get-capacity [_]
    capacity)
  
  (get-space [this]
    (- (.get-capacity this)
       (if-let [fluid (.get-fluid-stored this)]
         (.get-amount fluid)
         0)))
  
  (can-fill [_ fluid]
    (if-let [current-fluid (.get-fluid (:fluid @state-atom))]
      (= fluid current-fluid)
      (accepts-fluid fluid)))
  
  (can-drain [_ fluid]
    (when-let [stored (.get-fluid (:fluid @state-atom))]
      (= fluid (.get-fluid stored))))
  
  (fill [this fluid-stack simulate]
    (let [fluid (.get-fluid fluid-stack)
          amount (.get-amount fluid-stack)]
      (if (.can-fill this fluid)
        (let [space (.get-space this)
              fill-amount (min amount space)]
          (when-not simulate
            (if-let [current (:fluid @state-atom)]
              (swap! state-atom update :fluid #(.grow % fill-amount))
              (swap! state-atom assoc :fluid 
                     (->FluidStack fluid fill-amount))))
          fill-amount)
        0)))
  
  (drain [this amount simulate]
    (when-let [stored (:fluid @state-atom)]
      (let [drain-amount (min amount (.get-amount stored))]
        (when-not simulate
          (let [remaining (.shrink stored drain-amount)]
            (if (pos? (.get-amount remaining))
              (swap! state-atom assoc :fluid remaining)
              (swap! state-atom assoc :fluid nil))))
        (->FluidStack (.get-fluid stored) drain-amount)))))

;; Fluid handler implementation
(defrecord FluidHandler [tanks]
  IFluidHandler
  (get-tanks [_]
    (count tanks))
  
  (get-fluid-in-tank [_ tank]
    (.get-fluid-stored (get tanks tank)))
  
  (get-tank-capacity [_ tank]
    (.get-capacity (get tanks tank)))
  
  (fill-tank [_ tank fluid-stack simulate]
    (when-let [tank-obj (get tanks tank)]
      (.fill tank-obj fluid-stack simulate)))
  
  (drain-tank [_ tank amount simulate]
    (when-let [tank-obj (get tanks tank)]
      (.drain tank-obj amount simulate)))
  
  (is-fluid-valid [_ tank fluid]
    (when-let [tank-obj (get tanks tank)]
      (.can-fill tank-obj fluid))))

;; Factory functions
(defn create-fluid-stack [fluid amount]
  (->FluidStack fluid amount))

(defn create-fluid-tank [capacity & [fluid-filter]]
  (->FluidTank capacity 
               (or fluid-filter (constantly true))
               (atom {:fluid nil})))

(defn create-fluid-handler [& tanks]
  (->FluidHandler (vec tanks)))

;; Tank management
(defn register-tank! [tank-id tank]
  (swap! fluid-state assoc-in [:tanks tank-id] tank))

(defn get-tank [tank-id]
  (get-in @fluid-state [:tanks tank-id]))

;; Initialize fluid system
(defn init-fluids! []
  (reset! fluid-state {:tanks {}}))