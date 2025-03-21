(ns cn.academy.block.multiblock.multiblock-base
  (:require [mcmod.protocols :refer [IMultiblock]]
            [mcmod.direction :as dir]
            [mcmod.block :as block]))

(def MAX_BLOCKS 512)

(defprotocol IMultiblockMember
  "Protocol for multiblock structure members"
  (get-controller [this] "Get controller instance")
  (set-controller [this controller] "Set controller instance")
  (can-connect? [this other] "Check if can connect to other member")
  (get-member-type [this] "Get type identifier of member")
  (on-connection [this other] "Called when this member connects to another"))

(defprotocol IMultiblockHandler
  "Protocol for multiblock structure validation and management"
  (validate-structure [this world pos] "Validate multiblock structure at position")
  (on-structure-formed [this] "Called when structure is successfully formed")
  (on-structure-broken [this] "Called when structure is broken"))

(defn is-multiblock-part?
  "Check if block is part of a multiblock structure"
  [block]
  (satisfies? IMultiblockMember block))

(defn is-multiblock-master?
  "Check if block is a multiblock structure master"
  [block]
  (and (satisfies? IMultiblockMember block)
       (= "controller" (get-member-type block))))

(defn get-block-type
  "Get type of a multiblock block"
  [block]
  (when (satisfies? IMultiblockMember block)
    (get-member-type block)))

(defn get-block-facing
  "Get facing direction of a multiblock block"
  [block]
  (if (satisfies? mcmod.protocols/IDirectionalMachine block)
    (mcmod.protocols/get-facing block)
    (dir/north)))

(defn set-block-facing!
  "Set facing direction of a multiblock block"
  [block facing]
  (when (satisfies? mcmod.protocols/IDirectionalMachine block)
    (mcmod.protocols/set-facing! block facing)))

;; Multiblock helper functions
(defn find-connected-blocks
  "Find all connected blocks of a multiblock structure"
  [world pos]
  (loop [to-check #{pos}
         checked #{}
         blocks #{}]
    (if (empty? to-check)
      blocks
      (let [current (first to-check)
            te (block/get-tile-entity world current)]
        (if (and te (is-multiblock-part? te))
          (let [adjacent (dir/get-adjacent-positions current)
                new-blocks (conj blocks current)
                new-checked (conj checked current)
                new-to-check (into (disj to-check current)
                                  (remove checked 
                                    (filter #(when-let [adj-te (block/get-tile-entity world %)]
                                             (and (is-multiblock-part? adj-te)
                                                  (can-connect? te adj-te)))
                                           adjacent)))]
            (recur new-to-check new-checked new-blocks))
          (recur (disj to-check current)
                 (conj checked current)
                 blocks))))))