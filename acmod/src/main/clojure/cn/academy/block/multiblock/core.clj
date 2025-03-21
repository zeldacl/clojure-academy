(ns cn.academy.block.multiblock.core
  (:require [cn.academy.block.multiblock.registry :as registry]
            [cn.academy.block.multiblock.pattern :as pattern]
            [cn.academy.block.multiblock.interaction :as interaction]
            [cn.academy.block.multiblock.multiblock-base :as base]
            [mcmod.protocols :refer [ILifecycle]]
            [clojure.tools.logging :as log]))

;; Public API for structure management
(defn register-pattern!
  "Register a new multiblock structure pattern"
  [id blocks positions & {:keys [validation]}]
  (let [pattern (pattern/create-pattern id 
                  :blocks blocks
                  :positions positions
                  :validation validation)]
    (registry/register-pattern! pattern)
    pattern))

(defn validate-structure
  "Validate multiblock structure at position"
  [world pos pattern-id]
  (when-let [pattern (registry/get-pattern pattern-id)]
    (pattern/validate-structure pattern world pos)))

(defn try-form-structure!
  "Attempt to form multiblock structure at position"
  [world pos pattern-id]
  (registry/validate-and-create! world pos pattern-id))

;; Public API for block interaction
(defn handle-block-activation
  "Handle block right-click activation"
  [world pos player hand]
  (interaction/handle-block-activation world pos player hand))

(defn handle-block-broken
  "Handle block being broken"
  [world pos]
  (interaction/handle-block-broken world pos))

;; Public API for block implementation
(defn get-controller
  "Get controller for block"
  [block]
  (base/get-controller block))

(defn is-multiblock-complete?
  "Check if multiblock structure is complete"
  [controller]
  (when (satisfies? mcmod.protocols/IMultiblock controller)
    (mcmod.protocols/is-complete? controller)))

;; System lifecycle
(extend-type cn.academy.block.multiblock.core
  ILifecycle
  (start [_]
    (log/info "Starting multiblock system")  
    (registry/init-registry!))
  
  (stop [_]
    (log/info "Stopping multiblock system")))

;; Register for lifecycle management
(mcmod.lifecycle/register-lifecycle *ns*)