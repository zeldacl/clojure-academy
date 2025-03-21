(ns cn.academy.block.multiblock.multiblock-helper
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [mcmod.position :as position]))

(defn get-adjacent-blocks
  "Get positions of blocks adjacent to pos"
  [pos]
  (for [dx [-1 0 1]
        dy [-1 0 1]
        dz [-1 0 1]
        :when (not= [dx dy dz] [0 0 0])]
    (position/add pos dx dy dz)))

(defn find-connected-blocks
  "Find all connected blocks of a multiblock structure"
  [world start-pos max-blocks]
  (loop [to-check #{start-pos}
         checked #{}
         blocks #{}]
    (if (or (empty? to-check)
            (>= (count blocks) max-blocks))
      blocks
      (let [pos (first to-check)
            remaining (disj to-check pos)
            te (world/get-tile-entity world pos)]
        (if (and te (base/is-multiblock-part? te))
          (let [adjacent (get-adjacent-blocks pos)
                new-blocks (conj blocks pos)  
                new-checked (conj checked pos)
                new-to-check (into remaining
                                  (filter #(not (checked %))
                                         adjacent))]
            (recur new-to-check new-checked new-blocks))
          (recur remaining
                 (conj checked pos)
                 blocks))))))

(defn find-master-block
  "Find master block of a multiblock structure"
  [world blocks]
  (first 
    (filter #(when-let [te (world/get-tile-entity world %)]
              (base/is-multiblock-master? te))
            blocks)))

(defn validate-structure
  "Validate a multiblock structure"
  [world blocks]
  (and (<= (count blocks) base/MAX_BLOCKS)
       (every? #(when-let [te (world/get-tile-entity world %)]
                  (base/is-multiblock-part? te))
               blocks)
       (some #(when-let [te (world/get-tile-entity world %)]
                (base/is-multiblock-master? te))
             blocks)))

(defn break-structure
  "Break a multiblock structure, notifying all members"
  [world blocks]
  (doseq [pos blocks
          :let [block (world/get-tile-entity world pos)]
          :when (base/is-multiblock-part? block)]
    (base/set-controller block nil)
    (world/mark-dirty! block)))