(ns cn.academy.items
  (:require [mcmod.protocols :refer :all]
            [mcmod.registry :as mcr]
            [clojure.tools.logging :as log]
            [cn.academy.core :as core]))

;; Core item definitions 
(def core-items
  {"crystal_low" {:max-stack-size 64}
   "crystal_normal" {:max-stack-size 64}
   "crystal_pure" {:max-stack-size 64}
   "data_chip" {:max-stack-size 64}
   "developer_portable" {:max-stack-size 1
                        :max-damage 100}
   "energy_unit" {:max-stack-size 16}
   "calc_chip" {:max-stack-size 64}
   "info_component" {:max-stack-size 64}
   "resonance_component" {:max-stack-size 64}
   "matter_unit" {:max-stack-size 16}
   "matrix_core" {:max-stack-size 1}
   "magnetic_coil" {:max-stack-size 1
                    :max-damage 100}
   "terminal_installer" {:max-stack-size 1}})

(defrecord AcademyItem [properties]
  IItem
  (get-item-properties [this]
    properties)
  
  (get-max-stack-size [this]
    (:max-stack-size properties 64))
  
  (get-max-damage [this]
    (:max-damage properties 0))
  
  (get-creative-tab [this]
    (:creative-tab properties core/creative-tab))
  
  (has-effect? [this stack]
    (:has-effect properties false))
  
  (is-repairable [this]
    (:repairable properties false)))

(defn create-item 
  "Create a new item with given properties"
  [properties]
  (->AcademyItem properties))

;; Factory functions for specific items
(defn create-basic-item [props]
  (create-item (merge {:max-stack-size 64
                      :creative-tab core/creative-tab} 
                     props)))

(defn create-tool-item [props]
  (create-item (merge {:max-stack-size 1
                      :max-damage 100
                      :creative-tab core/creative-tab}
                     props)))

;; Registration functions
(defn register-items! [registry]
  (doseq [[item-id props] core-items]
    (register! registry :item item-id (create-basic-item props))))