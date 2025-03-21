(ns cn.academy.block.block.block-phase-gen
  (:require [cn.academy.block.behavior :as behavior]
            [cn.academy.block.properties :as props]
            [cn.academy.block.blocks :as blocks]))

(blocks/register-block-type!
  "phase_generator"
  {:properties (merge (props/get-default-properties :energy-machine)
                     {:hardness 4.5
                      :resistance 10.0})
   :constructor (fn [props]
                 (let [state (atom {:energy 0
                                  :active false
                                  :efficiency 1.0})
                       config {:max-output 200
                              :base-efficiency 0.9
                              :energy-capacity 10000}]
                   {:type :phase-generator
                    :properties props
                    :behaviors (behavior/combine-behaviors :energy-producer)
                    :state state
                    :config config}))})