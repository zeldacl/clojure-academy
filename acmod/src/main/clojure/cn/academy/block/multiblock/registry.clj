(ns cn.academy.block.multiblock.registry
  (:require [cn.academy.block.multiblock.pattern :as pattern]
            [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.multiblock-controller :as controller]
            [mcmod.protocols :refer [ILifecycle]]
            [mcmod.lifecycle :as lifecycle]
            [clojure.tools.logging :as log]))

;; Registry state
(def registry-state
  (atom {:patterns {}
         :active-structures {}}))

;; Pattern registration
(defn register-pattern!
  "Register a new multiblock pattern"
  [pattern]
  (swap! registry-state assoc-in [:patterns (:id pattern)] pattern))

(defn get-pattern
  "Get registered pattern by ID"
  [id]
  (get-in @registry-state [:patterns id]))

;; Structure registration and tracking
(defn register-active-structure!
  "Register an active multiblock structure"
  [controller]
  (let [pos (mcmod.protocols/get-position controller)]
    (swap! registry-state assoc-in [:active-structures pos] controller)))

(defn unregister-active-structure!
  "Unregister an active multiblock structure"
  [controller]
  (let [pos (mcmod.protocols/get-position controller)]
    (swap! registry-state update :active-structures dissoc pos)))

(defn get-active-structure
  "Get active structure at position"
  [pos]
  (get-in @registry-state [:active-structures pos]))

;; Structure validation and creation
(defn validate-and-create!
  "Validate structure and create controller if valid"
  [world pos pattern-id]
  (when-let [pattern (get-pattern pattern-id)]
    (when (pattern/validate-structure pattern world pos)
      (let [controller (controller/->MultiblockController 
                        (atom {:active false
                              :members #{}})
                        #(pattern/matches-pattern? pattern %))]
        (register-active-structure! controller)
        controller))))

;; Registry lifecycle management
(extend-type clojure.lang.Atom
  ILifecycle
  (start [this]
    (log/info "Starting multiblock registry")
    (reset! this {:patterns {}
                  :active-structures {}}))
  
  (stop [this]
    (log/info "Stopping multiblock registry")
    (reset! this {:patterns {}
                  :active-structures {}})))

;; Register for lifecycle management
(lifecycle/register-lifecycle registry-state)

;; Initialize with default patterns
(defn init-registry! []
  ;; Add default patterns here if needed
  (log/info "Initialized multiblock registry"))