(ns cn.academy.block.multiblock.multiblock-base
  (:require [cn.academy.block.core :as core]
            [cn.academy.api.block :as block-api]))

(defprotocol IMultiblockController
  "Protocol for managing multiblock structures"
  (is-complete? [this] "Check if structure is complete")
  (get-blocks [this] "Get all blocks in structure")
  (get-master [this] "Get master/controller block")
  (validate-structure [this] "Validate multiblock structure")
  (on-structure-formed [this] "Called when structure is successfully formed")
  (on-structure-broken [this] "Called when structure is broken"))

(defprotocol IMultiblockMember
  "Protocol for blocks that can be part of a multiblock"
  (get-controller [this] "Get associated controller")
  (set-controller [this controller] "Set associated controller")
  (can-connect? [this other] "Check if can connect to another block")
  (on-connection [this other] "Called when connected to another block")
  (get-multiblock-data [this] "Get data relevant to multiblock structure"))

(defrecord MultiblockState [controller-pos members active]
  core/IBlockEntity
  (load-data [this data]
    (assoc this
           :controller-pos (:controller data)
           :members (:members data)
           :active (:active data false)))
  
  (save-data [this]
    {:controller (:controller-pos this)
     :members (:members this)
     :active (:active this)}))