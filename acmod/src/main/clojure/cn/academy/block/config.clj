(ns cn.academy.block.config
  (:require [mcmod.protocols.block :as block-protocol]))

(def base-block-properties
  {:metal {:hardness 4.0
           :resistance 8.0
           :has-tile-entity true
           :harvest-level 1}
   :machine {:hardness 3.5 
             :resistance 17.5
             :has-tile-entity true}
   :ore {:hardness 3.0
         :resistance 5.0
         :harvest-level 2}})

(def block-config
  {"wireless_matrix" (merge (:metal base-block-properties)
                           {:light-level 7})
   "energy_generator" (merge (:machine base-block-properties)
                           {:light-level 7})
   "cat_engine" (merge (:metal base-block-properties)
                      {:light-level 0})
   "energy_storage" (merge (:machine base-block-properties)
                         {:light-level 0})
   "crystal_ore" (:ore base-block-properties)})

(defn get-block-properties [block-id]
  (get block-config block-id))