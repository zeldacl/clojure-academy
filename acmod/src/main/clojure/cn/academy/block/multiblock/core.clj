(ns cn.academy.block.multiblock.core
  (:require [cn.academy.block.behavior :as behavior]
            [clojure.tools.logging :as log]))

;; Multiblock structure management
(defn create-multiblock
  "Create a new multiblock structure definition"
  [id {:keys [validator controller-type member-types]}]
  {:id id
   :validator validator
   :controller-type controller-type
   :member-types member-types})

(defn register-structure!
  "Register a multiblock structure type"
  [structure-def]
  (mcmod.multiblock/register-structure! structure-def))

;; Multiblock member creation helpers
(defn create-member
  "Create a multiblock member with basic functionality"
  [member-type state-atom]
  {:type member-type
   :state-atom state-atom
   :behaviors (behavior/combine-behaviors :multiblock-member)})

(defn create-controller
  "Create a multiblock controller with validation and management capabilities"
  [structure-id state-atom]
  {:type :controller
   :structure-id structure-id
   :state-atom state-atom
   :behaviors (behavior/combine-behaviors :multiblock-member :machine)})

;; Structure validation and management
(defn validate-structure
  "Validate a multiblock structure configuration"
  [world pos structure-id]
  (mcmod.multiblock/validate-structure world pos structure-id))

(defn update-structure!
  "Update a multiblock structure state"
  [world pos state-update-fn]
  (mcmod.multiblock/update-structure! world pos state-update-fn))

;; Export common multiblock behaviors
(def handle-block-broken mcmod.multiblock/handle-block-broken)
(def handle-block-placed mcmod.multiblock/handle-block-placed)
(def handle-block-activated mcmod.multiblock/handle-block-activated)