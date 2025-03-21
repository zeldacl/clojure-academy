(ns cn.academy.block.multiblock.multiblock-base
  (:require [mcmod.direction :as dir]
            [mcmod.block :as block]
            [mcmod.protocols :refer [IMultiblock]]))

(def MAX_BLOCKS 512)

(defprotocol IMultiblockMember
  "Protocol for multiblock structure members"
  (get-controller [this] "Get controller instance")
  (set-controller [this controller] "Set controller instance")
  (can-connect? [this other] "Check if can connect to other member")
  (get-member-type [this] "Get type identifier of member"))

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