(ns cn.academy.block.multiblock.registry
  (:require [mcmod.protocols :refer [ILifecycle]]
            [mcmod.lifecycle :as lifecycle]
            [mcmod.logging :as log]
            [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.multiblock-helper :as helper]))

;; Registry of multiblock structure validators
(def structure-validators (atom {}))

;; Registry of active multiblock structures
(def active-structures (atom {}))

(defn register-structure-type!
  "Register a new multiblock structure type with validation"
  [type-id validator]
  (swap! structure-validators assoc type-id validator))

(defn get-structure-validator
  "Get validator function for structure type"
  [type-id]
  (get @structure-validators type-id))

(defn register-active-structure!
  "Register an active multiblock structure"
  [controller]
  (let [pos (base/get-master controller)]
    (swap! active-structures assoc pos controller)))

(defn unregister-active-structure!
  "Unregister an active multiblock structure"
  [controller]
  (let [pos (base/get-master controller)]
    (swap! active-structures dissoc pos)))

(defn get-active-structure
  "Get active structure at position"
  [pos]
  (get @active-structures pos))

(defn validate-and-register!
  "Validate structure and register if valid"
  [world pos type-id]
  (when-let [validator (get-structure-validator type-id)]
    (when-let [structure (helper/validate-structure world pos validator)]
      (register-active-structure! structure)
      structure)))

(extend-type clojure.lang.Atom
  ILifecycle
  (start [this]
    (log/info "Starting multiblock registry")
    (reset! this {}))
  
  (stop [this]
    (log/info "Stopping multiblock registry")
    (reset! this {})))

;; Register for lifecycle management
(lifecycle/register-lifecycle active-structures)