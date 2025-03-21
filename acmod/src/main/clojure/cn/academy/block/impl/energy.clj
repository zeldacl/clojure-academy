(ns cn.academy.block.impl.energy
  (:require [cn.academy.block.properties :as props]
            [cn.academy.block.behavior :as behavior]
            [cn.academy.block.blocks :as blocks]
            [cn.academy.block.multiblock.core :as multiblock]))

;; Energy producer blocks
(def energy-blocks
  {"cat_engine" {:max-output 100
                 :base-efficiency 0.8}
   "phase_generator" {:max-output 200
                     :base-efficiency 0.9}
   "solar_generator" {:max-output 50
                     :base-efficiency 1.0}
   "wind_generator" {:max-output 75
                    :base-efficiency 0.85}})

(defn create-energy-block [id config]
  (let [base-props (props/get-default-properties :energy-machine)
        behaviors (behavior/combine-behaviors :energy-producer)
        state (atom {:energy 0
                    :active false})]
    {:id id
     :properties base-props
     :behaviors behaviors
     :state state
     :config config}))

;; Register all energy producer blocks
(doseq [[id config] energy-blocks]
  (blocks/register-block-type!
    id
    {:properties (props/get-default-properties :energy-machine)
     :constructor #(create-energy-block id config)}))