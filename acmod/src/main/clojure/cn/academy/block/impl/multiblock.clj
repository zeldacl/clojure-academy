(ns cn.academy.block.impl.multiblock
  (:require [cn.academy.block.multiblock.core :as multiblock]
            [cn.academy.block.multiblock.pattern :as pattern]
            [cn.academy.block.multiblock.registry :as registry]
            [cn.academy.block.multiblock.multiblock-member :as member]
            [cn.academy.block.properties :as props]
            [cn.academy.block.blocks :as blocks]))

;; Define standard multiblock patterns
(def structure-patterns
  {"processing_machine" 
   (pattern/create-pattern "processing_machine"
     :blocks [:controller :casing :casing :casing :casing
              :casing :casing :casing :casing :energy-port]
     :positions [[0 0 0] [1 0 0] [-1 0 0] [0 1 0] [0 -1 0]
                 [0 0 1] [0 0 -1] [1 1 0] [-1 1 0] [0 1 1]]
     :validation #(every? (fn [block]
                          (contains? #{:controller :casing :energy-port}
                                    (:type block)))
                        %))})

;; Register patterns
(doseq [[id pattern] structure-patterns]
  (registry/register-pattern! pattern))

;; Create multiblock member blocks
(defn create-multiblock-member [member-type]
  (let [base-props (props/get-default-properties :machine)
        valid-connections (member/get-valid-connections member-type)]
    {:properties base-props
     :member (member/create-member member-type 
                                 :valid-connections valid-connections)}))

;; Register block types
(def member-blocks
  {"mb_controller" :controller
   "mb_casing" :casing
   "mb_energy_port" :energy-port})

(doseq [[id member-type] member-blocks]
  (blocks/register-block-type!
    id
    {:properties (props/get-default-properties :machine)
     :constructor #(create-multiblock-member member-type)}))