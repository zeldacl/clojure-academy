(ns cn.academy.block.multiblock.validation
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.multiblock-helper :as helper]
            [cn.academy.api.block :as block-api]
            [cn.academy.block.multiblock.pattern :as pattern])
  (:import [net.minecraft.util.math BlockPos]
           [net.minecraft.world World]))

(defprotocol IStructureValidator
  "Protocol for validating multiblock structures"
  (validate [this blocks] "Validate the structure formed by blocks")
  (get-requirements [this] "Get block type requirements")
  (get-error [this blocks] "Get error message if invalid"))

(defrecord StructureValidator [requirements validator]
  IStructureValidator
  (validate [_ blocks]
    (validator blocks))
  
  (get-requirements [_]
    requirements)
  
  (get-error [_ blocks]
    (let [block-types (frequencies 
                       (map base/get-member-type blocks))] 
      (when-not (= (count blocks)
                   (apply + (vals requirements)))
        "Wrong number of blocks")
      (when-let [[type count] (first
                               (filter (fn [[type req-count]]
                                       (not= req-count
                                            (get block-types type 0)))
                                     requirements))] 
        (format "Expected %d blocks of type %s, found %d"
                count type (get block-types type 0))))))

(defn create-validator
  "Create a new structure validator"
  [requirements validator]
  (->StructureValidator requirements validator))

(defn check-block-types
  "Helper to validate block type counts"
  [blocks requirements]
  (let [block-types (frequencies 
                     (map base/get-member-type blocks))]
    (every? (fn [[type count]]
              (= count (get block-types type 0)))
            requirements)))

(defn check-dimensions
  "Validate structure dimensions"
  [blocks min-size max-size]
  (let [positions (map (fn [pos]
                        [(.-x pos) (.-y pos) (.-z pos)])
                      blocks)
        min-pos (map #(apply min %) (apply map vector positions))
        max-pos (map #(apply max %) (apply map vector positions))
        dimensions (map - max-pos min-pos)]
    (every? #(<= %1 %2 %3) min-size dimensions max-size)))

(defn check-pattern
  "Validate structure matches pattern"
  [world blocks pattern]
  (let [positions (set blocks)]
    (every? (fn [{:keys [pos type]}]
              (when-let [te (block-api/get-tile-entity world pos)]
                (= (base/get-member-type te) type)))
            pattern)))

(defprotocol IMultiblockValidator
  (validate-at [this world pos] "Validate multiblock structure at given position")
  (get-size [this] "Get size requirements [width height depth]")
  (get-required-blocks [this] "Get map of required block types and counts"))

(defrecord MultiblockValidator [pattern size required-blocks]
  IMultiblockValidator
  (validate-at [_ world pos]
    (let [[width height depth] size
          blocks (for [x (range width)
                      y (range height) 
                      z (range depth)]
                  (let [check-pos (-> pos
                                    (.add x y z))
                        block (.getBlockState world check-pos)]
                    [x y z block]))]
      (pattern/matches-pattern? pattern blocks)))
      
  (get-size [_] size)
  
  (get-required-blocks [_] required-blocks))

(defn create-validator
  "Create new multiblock validator from pattern"
  [pattern]
  (let [size (pattern/get-size pattern)
        required (pattern/get-required-blocks pattern)]
    (->MultiblockValidator pattern size required)))

(defn find-complete-structure
  "Search for complete multiblock structure around position"
  [validator world pos]
  (let [[width height depth] (get-size validator)]
    (first
     (for [x (range (- width) 1)
           y (range (- height) 1)
           z (range (- depth) 1)
           :let [check-pos (-> pos
                             (.add x y z))
                 valid? (validate-at validator world check-pos)]
           :when valid?]
       check-pos))))