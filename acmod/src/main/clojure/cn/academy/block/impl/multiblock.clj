(ns cn.academy.block.impl.multiblock
  (:require [cn.academy.block.multiblock.core :as multiblock]
            [cn.academy.block.properties :as props]
            [cn.academy.block.behavior :as behavior]
            [cn.academy.block.blocks :as blocks]))

;; Define multiblock structure types
(def structure-types
  {"processing_machine" {:validator (fn [blocks]
                                    (let [counts (frequencies (map :type blocks))]
                                      (and (= (get counts :controller 0) 1)
                                           (>= (get counts :casing 0) 8)
                                           (= (get counts :energy-port 0) 1))))
                        :controller-type :processor
                        :member-types #{:controller :casing :energy-port}}})

;; Register all multiblock structures
(doseq [[id config] structure-types]
  (multiblock/register-structure!
    (multiblock/create-multiblock id config)))

;; Create multiblock member blocks
(defn create-multiblock-member [member-type]
  (let [base-props (props/get-default-properties :machine)
        state (atom {:controller nil})
        member (multiblock/create-member member-type state)]
    {:properties base-props
     :member member}))

;; Register multiblock member blocks
(def member-blocks
  {"mb_controller" :controller
   "mb_casing" :casing
   "mb_energy_port" :energy-port})

(doseq [[id member-type] member-blocks]
  (blocks/register-block-type!
    id
    {:properties (props/get-default-properties :machine)
     :constructor #(create-multiblock-member member-type)}))