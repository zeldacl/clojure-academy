(ns cn.academy.block.multiblock.validation
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.pattern :as pattern]
            [mcmod.block :as block]
            [mcmod.position :as position]))

(defprotocol IStructureValidator
  "Protocol for structure validation"
  (validate [this blocks] "Validate block structure")
  (get-requirements [this] "Get structure requirements")
  (get-patterns [this] "Get valid structure patterns"))

(defrecord StructureValidator [requirements patterns]
  IStructureValidator
  (validate [_ blocks]
    (and (<= (count blocks) base/MAX_BLOCKS)
         (check-block-counts blocks requirements)
         (some #(pattern/matches-pattern? % blocks) patterns)))
  
  (get-requirements [_]
    requirements)
  
  (get-patterns [_]
    patterns))

(defn check-block-counts
  "Check if block counts match requirements"
  [blocks requirements]
  (let [counts (frequencies 
                (map base/get-member-type 
                     (filter base/is-multiblock-part? blocks)))]
    (every? (fn [[type required]]
              (>= (get counts type 0) required))
            requirements)))

(defn create-validator
  "Create a new structure validator"
  ([requirements]
   (create-validator requirements []))
  ([requirements patterns]
   (->StructureValidator requirements patterns)))

(defn add-pattern
  "Add a valid structure pattern"
  [validator pattern]
  (update validator :patterns conj pattern))

(defn remove-pattern
  "Remove a structure pattern"
  [validator pattern]
  (update validator :patterns 
          (fn [patterns]
            (remove #(= % pattern) patterns))))

(defn check-block-positions
  "Validate relative block positions"
  [blocks]
  (let [positions (map :pos blocks)
        [min-x min-y min-z] (map #(apply min %) 
                                (apply map vector positions))
        [max-x max-y max-z] (map #(apply max %)
                                (apply map vector positions))]
    (and (<= (- max-x min-x) 5)
         (<= (- max-y min-y) 5)
         (<= (- max-z min-z) 5))))

(defn check-connected
  "Check if blocks form connected structure"
  [blocks]
  (let [positions (set (map :pos blocks))]
    (loop [to-check #{(first positions)}
           checked #{}]
      (if (empty? to-check)
        (= checked positions)
        (let [current (first to-check)
              neighbors (for [offset [[1 0 0] [-1 0 0] 
                                    [0 1 0] [0 -1 0]
                                    [0 0 1] [0 0 -1]]
                            :let [pos (position/add current offset)]
                            :when (positions pos)]
                        pos)]
          (recur (into (disj to-check current)
                      (remove checked neighbors))
                 (conj checked current)))))))