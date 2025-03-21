(ns cn.academy.block.multiblock.multiblock-base
  (:require [mcmod.direction :as dir]
            [mcmod.block :as block]))

(def MAX_BLOCKS 512)

(defprotocol IMultiblock
  "Base protocol for multiblock structure controllers"
  (is-complete? [this] "Check if structure is complete")
  (validate [this blocks] "Validate structure formed by blocks")
  (get-parts [this] "Get all member blocks"))

(defprotocol IMultiblockMember
  "Protocol for multiblock structure members"
  (get-controller [this] "Get controller instance")
  (set-controller [this controller] "Set controller instance")
  (can-connect? [this other] "Check if can connect to other member")
  (get-member-type [this] "Get type identifier of member"))

(defprotocol IDirectionalMachine
  "Protocol for machines with facing direction"
  (get-facing [this] "Get current facing direction")
  (set-facing! [this facing] "Set facing direction"))

(defprotocol IStructuredMachine
  "Protocol for machines with internal structure"
  (get-structure [this] "Get machine structure definition")
  (validate-structure [this blocks] "Validate machine structure")
  (update-structure [this blocks] "Update machine structure"))

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
  (if (satisfies? IDirectionalMachine block)
    (get-facing block)
    (dir/north)))

(defn set-block-facing!
  "Set facing direction of a multiblock block"
  [block facing]
  (when (satisfies? IDirectionalMachine block)
    (set-facing! block facing)))