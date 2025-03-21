(ns cn.academy.block.impl.processor
  (:require [cn.academy.block.properties :as props]
            [cn.academy.block.behavior :as behavior]
            [cn.academy.block.blocks :as blocks]))

;; Processor block configurations
(def processor-blocks
  {"metal_former" {:energy-capacity 2000
                   :work-speed 1.0
                   :energy-per-tick 20}
   "imag_fusor" {:energy-capacity 5000
                 :work-speed 0.8
                 :energy-per-tick 50}
   "developer" {:energy-capacity 1000
                :work-speed 1.2
                :energy-per-tick 10}})

(defn create-processor [id config]
  (let [base-props (props/get-default-properties :processor)
        behaviors (behavior/combine-behaviors :machine)
        state (atom {:energy 0
                    :progress 0
                    :active false
                    :current-recipe nil})]
    {:id id
     :properties base-props
     :behaviors behaviors
     :state state
     :config config}))

;; Register all processor blocks
(doseq [[id config] processor-blocks]
  (blocks/register-block-type!
    id
    {:properties (props/get-default-properties :processor)
     :constructor #(create-processor id config)}))