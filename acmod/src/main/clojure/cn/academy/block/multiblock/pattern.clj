(ns cn.academy.block.multiblock.pattern
  (:require [mcmod.position :as position]
            [mcmod.block :as block]))

(defprotocol IMultiblockPattern
  "Protocol for multiblock structure patterns"
  (matches-pattern? [this blocks] "Check if blocks match pattern")
  (get-size [this] "Get pattern dimensions")
  (get-required-blocks [this] "Get required block counts"))

(defrecord MultiblockPattern [blocks size requirements]
  IMultiblockPattern
  (matches-pattern? [_ blocks]
    (every? (fn [[pos expected-type]]
              (when-let [block (get blocks pos)]
                (= expected-type (block/get-type block))))
            blocks))
  
  (get-size [_] size)
  
  (get-required-blocks [_] requirements))

(defn create-pattern
  "Create a new multiblock pattern"
  [blocks size requirements]
  (->MultiblockPattern blocks size requirements))

(defn get-block-counts
  "Count blocks by type in pattern"
  [pattern]
  (frequencies (map second (:blocks pattern))))

(defn get-pattern-size
  "Get size of pattern in blocks"
  [pattern]
  (let [positions (map first (:blocks pattern))
        xs (map position/get-x positions)
        ys (map position/get-y positions)
        zs (map position/get-z positions)]
    [(- (apply max xs) (apply min xs))
     (- (apply max ys) (apply min ys))
     (- (apply max zs) (apply min zs))]))

(defn create-cubic-pattern
  "Create a cubic pattern with given dimensions"
  [width height depth block-type]
  (let [blocks (for [x (range width)
                    y (range height)
                    z (range depth)]
                [[x y z] block-type])
        size [width height depth]
        requirements {block-type (* width height depth)}]
    (create-pattern blocks size requirements)))

(defn rotate-pattern
  "Rotate pattern around Y axis"
  [pattern rotation]
  (let [blocks (map (fn [[pos type]]
                     [(position/rotate-y pos rotation) type])
                   (:blocks pattern))]
    (create-pattern blocks 
                   (:size pattern)
                   (:requirements pattern))))