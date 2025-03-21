(ns cn.academy.block.multiblock.pattern
  (:require [mcmod.position :as position]
            [mcmod.block :as block]
            [cn.academy.block.multiblock.multiblock-base :as base]))

(defprotocol IStructurePattern
  "Protocol for multiblock structure patterns"
  (matches-pattern? [this blocks] "Check if blocks match pattern")
  (get-pattern-blocks [this] "Get required block types and positions")
  (validate-structure [this world pos] "Validate structure at position"))

(defrecord MultiblockPattern [id blocks relative-positions validation-fn]
  IStructurePattern
  (matches-pattern? [_ block-map]
    (and (= (count blocks) (count block-map))
         (every? (fn [[block-type [dx dy dz]]]
                  (let [check-pos (position/offset {:x dx :y dy :z dz})
                        block (get block-map check-pos)]
                    (= (:type block) block-type)))
                (map vector blocks relative-positions))))
  
  (get-pattern-blocks [_]
    (map vector blocks relative-positions))
  
  (validate-structure [this world pos]
    (let [connected (base/find-connected-blocks world pos)]
      (and (<= (count connected) base/MAX_BLOCKS)
           (matches-pattern? this connected)
           (if validation-fn
             (validation-fn connected)
             true)))))

(defn create-pattern
  "Create a new multiblock pattern"
  [id & {:keys [blocks positions validation]}]
  (->MultiblockPattern id blocks positions validation))

;; Pattern registration
(def pattern-registry (atom {}))

(defn register-pattern!
  "Register a multiblock pattern"
  [pattern]
  (swap! pattern-registry assoc (:id pattern) pattern))

(defn get-pattern
  "Get registered pattern by ID"
  [id]
  (get @pattern-registry id))