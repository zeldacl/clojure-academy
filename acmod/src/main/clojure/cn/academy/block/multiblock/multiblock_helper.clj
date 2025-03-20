(ns cn.academy.block.multiblock.multiblock-helper
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.api.block :as block-api])
  (:import [net.minecraft.util.math BlockPos]))

(defn get-adjacent-positions
  "Get all adjacent block positions"
  [pos]
  (for [dx [-1 0 1]
        dy [-1 0 1]
        dz [-1 0 1]
        :when (not= 0 dx dy dz)]
    (BlockPos. (+ (.getX pos) dx)
               (+ (.getY pos) dy)
               (+ (.getZ pos) dz))))

(defn find-connected-blocks
  "Find all blocks that are part of the same multiblock structure"
  [world start-pos]
  (loop [to-check #{start-pos}
         checked #{}
         connected #{}]
    (if (empty? to-check)
      connected
      (let [current (first to-check)
            current-block (block-api/get-tile-entity world current)]
        (if (satisfies? base/IMultiblockMember current-block)
          (let [adjacent (remove checked (get-adjacent-positions current))
                valid-adjacent (filter #(when-let [block (block-api/get-tile-entity world %)]
                                       (and (satisfies? base/IMultiblockMember block)
                                            (base/can-connect? current-block block)))
                                    adjacent)]
            (recur (into (disj to-check current) valid-adjacent)
                   (conj checked current)
                   (conj connected current)))
          (recur (disj to-check current)
                 (conj checked current)
                 connected))))))

(defn validate-structure
  "Validate a multiblock structure from a starting position"
  [world pos validator]
  (let [connected (find-connected-blocks world pos)]
    (validator world connected)))

(defn break-structure
  "Break a multiblock structure, notifying all members"
  [world blocks]
  (doseq [pos blocks
          :let [block (block-api/get-tile-entity world pos)]
          :when (satisfies? base/IMultiblockMember block)]
    (base/set-controller block nil)
    (block-api/mark-dirty! block)))