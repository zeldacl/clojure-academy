(ns cn.academy.block.multiblock.multiblock-controller
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.core :as core]
            [cn.academy.protocols.block :as block-api]
            [mcmod.protocols :refer [IMultiblock IBlockEntity]]))

(defrecord MultiblockController [state-atom validator]
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

  base/IMultiblockHandler
  (validate-structure [this world pos]
    (let [connected (base/find-connected-blocks world pos)]
      (and (<= (count connected) base/MAX_BLOCKS)
           (validator connected))))
  
  (on-structure-formed [this]
    (swap! state-atom assoc :active true)
    (block-api/mark-dirty! this))
  
  (on-structure-broken [this]
    (let [{:keys [members]} @state-atom]
      (doseq [pos members
              :let [member (block-api/get-tile-entity 
                           (block-api/get-world this) 
                           pos)]]
        (when member
          (base/set-controller member nil)))
      (swap! state-atom assoc 
             :active false
             :members #{})))

  base/IMultiblockMember
  (get-controller [this]
    (get-controller this))
  
  (set-controller [this controller]
    (when controller
      (swap! state-atom assoc :controller-pos (base/get-controller controller))))
  
  (can-connect? [this other]
    true)
  
  (get-member-type [_]
    "controller")
  
  (on-connection [this other]
    (swap! state-atom update :members conj (block-api/get-position other)))

  IBlockEntity
  (load-data [this data]
    (reset! state-atom data))
  
  (save-data [this]
    @state-atom)
  
  (get-capabilities [this]
    (when (is-complete? this)
      (:capabilities @state-atom))))