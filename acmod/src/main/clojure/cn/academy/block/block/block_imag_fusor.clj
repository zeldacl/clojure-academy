(ns cn.academy.block.block.block-imag-fusor
  (:require [cn.academy.block.behavior :as behavior]
            [cn.academy.block.properties :as props]
            [cn.academy.block.blocks :as blocks]))

(blocks/register-block-type!
  "imag_fusor"
  {:properties (merge (props/get-default-properties :processor)
                     {:hardness 6.0
                      :resistance 15.0})
   :constructor (fn [props]
                 (let [state (atom {:energy 0
                                  :progress 0
                                  :active false
                                  :current-recipe nil
                                  :efficiency 1.0})
                       config {:energy-capacity 5000
                              :energy-per-tick 50
                              :work-speed 0.8
                              :inventory-size 4}]
                   {:type :imag-fusor
                    :properties props
                    :behaviors (behavior/combine-behaviors :machine :processor)
                    :state state
                    :config config}))})