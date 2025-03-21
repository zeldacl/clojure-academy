(ns cn.academy.block.multiblock.multiblock-controller
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.multiblock-helper :as helper]
            [cn.academy.block.core :as core]
            [cn.academy.api.block :as block-api]
            [mcmod.protocols :refer [IMultiblock]]))

(defrecord MultiblockController [state-atom validator]
  ;; Implement mcmod's IMultiblock protocol
  IMultiblock
  (is-complete? [this]
    (let [{:keys [members]} @state-atom]
      (validator members)))
  
  (get-blocks [this]
    (:members @state-atom))
  
  (get-controller [this]
    (block-api/get-tile-entity 
      (block-api/get-world this) 
      (:controller-pos @state-atom)))
  
  (validate-structure [this]
    (when-let [world (block-api/get-world this)]
      (helper/validate-structure world 
                               (:controller-pos @state-atom)
                               validator)))
  
  (on-structure-formed [this]
    (swap! state-atom assoc :active true)
    (block-api/mark-dirty! this))
  
  (on-structure-broken [this]
    (let [{:keys [members]} @state-atom]
      (helper/break-structure (block-api/get-world this) members)
      (swap! state-atom assoc 
             :active false
             :members #{})))

  ;; Add method to get master position for internal use
  base/IMultiblockMember
  (get-controller [this]
    (get-controller this))
  
  (set-controller [this controller]
    (when controller
      (swap! state-atom assoc :controller-pos (base/get-controller controller))))
  
  (can-connect? [this other]
    true)
  
  (on-connection [this other]
    (swap! state-atom update :members conj (block-api/get-position other)))
  
  (get-multiblock-data [this]
    @state-atom)

  core/IBlockEntity
  (load-data [this data]
    (reset! state-atom (base/map->MultiblockState data)))
  
  (save-data [this]
    (base/save-data @state-atom))
  
  (get-capabilities [this]
    ;; Return capabilities based on structure completion
    (when (is-complete? this)
      (:capabilities @state-atom)))
  
  (mark-dirty [this]
    (block-api/mark-dirty! this)))