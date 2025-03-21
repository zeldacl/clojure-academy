(ns cn.academy.block.block.block-ability-interferer
  (:require [cn.academy.block.behavior :as behavior]
            [cn.academy.block.properties :as props]
            [cn.academy.block.blocks :as blocks]))

;; Register ability interferer using new system
(blocks/register-block-type!
  "ability_interferer"
  {:properties (merge (props/get-default-properties :machine)
                     {:has-tile-entity true
                      :hardness 3.5
                      :resistance 8.0})
   :constructor (fn [props]
                 (let [state (atom {:range 0
                                  :active false})
                       config {:range-base 8
                              :range-boost 4}]
                   {:type :ability-interferer
                    :properties props
                    :behaviors (behavior/combine-behaviors :machine)
                    :state state
                    :config config}))})