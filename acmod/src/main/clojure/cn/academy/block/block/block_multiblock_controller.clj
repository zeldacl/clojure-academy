(ns cn.academy.block.block.block-multiblock-controller
  (:require [cn.academy.block.behavior :as behavior]
            [cn.academy.block.properties :as props]
            [cn.academy.block.blocks :as blocks]
            [cn.academy.block.multiblock.core :as multiblock]))

(blocks/register-block-type!
  "matrix_core"
  {:properties (merge (props/get-default-properties :machine)
                     {:hardness 8.0
                      :resistance 20.0
                      :has-tile-entity true})
   :constructor (fn [props]
                 (let [state (atom {:formed false
                                  :members #{}
                                  :energy 0})
                       config {:structure-id "matrix"
                              :energy-capacity 100000}]
                   {:type :matrix-controller
                    :properties props
                    :behaviors (behavior/combine-behaviors :machine :multiblock-member)
                    :member (multiblock/create-controller "matrix" state)
                    :state state
                    :config config}))})

(blocks/register-block-type!
  "matrix_component"
  {:properties (merge (props/get-default-properties :machine)
                     {:hardness 6.0
                      :resistance 15.0
                      :has-tile-entity true})
   :constructor (fn [props]
                 (let [state (atom {:controller nil})]
                   {:type :matrix-component
                    :properties props
                    :behaviors (behavior/combine-behaviors :multiblock-member)
                    :member (multiblock/create-member :component state)
                    :state state}))})