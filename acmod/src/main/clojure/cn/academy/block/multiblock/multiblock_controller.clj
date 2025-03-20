(ns cn.academy.block.multiblock.multiblock-controller
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.multiblock-helper :as helper]
            [cn.academy.block.core :as core]
            [cn.academy.api.block :as block-api]))

(defrecord MultiblockController [state-atom validator]
  base/IMultiblockController
  (is-complete? [this]
    (let [{:keys [members]} @state-atom]
      (validator members)))
  
  (get-blocks [this]
    (:members @state-atom))
  
  (get-master [this]
    (:controller-pos @state-atom))
  
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

  core/IBlockEntity
  (load-data [this data]
    (reset! state-atom (base/map->MultiblockState data)))
  
  (save-data [this]
    (base/save-data @state-atom))
  
  (get-capabilities [this]
    ;; Return capabilities based on structure completion
    (when (base/is-complete? this)
      (:capabilities @state-atom)))
  
  (mark-dirty [this]
    (block-api/mark-dirty! this)))