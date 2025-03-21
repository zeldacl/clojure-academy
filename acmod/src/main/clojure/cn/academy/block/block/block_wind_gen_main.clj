(ns cn.academy.block.block.block-wind-gen-main
  (:require [cn.academy.block.behavior :as behavior]
            [cn.academy.block.properties :as props]
            [cn.academy.block.blocks :as blocks]
            [clojure.tools.logging :as log]))

;; Register wind generator using new system
(blocks/register-block-type!
  "wind_generator"
  {:properties (props/get-default-properties :energy-machine)
   :constructor (fn [props]
                 (let [state (atom {:energy 0
                                  :active false})
                       config {:max-output 75
                              :base-efficiency 0.85}]
                   {:type :wind-generator
                    :properties props
                    :behaviors (behavior/combine-behaviors :energy-producer)
                    :state state
                    :config config}))}))