(ns cn.academy.block.block.block-metal-former
  (:require [cn.academy.block.behavior :as behavior]
            [cn.academy.block.properties :as props]
            [cn.academy.block.blocks :as blocks]))

(blocks/register-block-type!
  "metal_former"
  {:properties (merge (props/get-default-properties :processor)
                     {:hardness 5.0
                      :resistance 12.0})
   :constructor (fn [props]
                 (let [state (atom {:energy 0
                                  :progress 0
                                  :active false
                                  :current-recipe nil})
                       config {:energy-capacity 2000
                              :energy-per-tick 20
                              :work-speed 1.0
                              :inventory-size 3}]
                   {:type :metal-former
                    :properties props
                    :behaviors (behavior/combine-behaviors :machine :processor)
                    :state state
                    :config config}))})