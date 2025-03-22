(ns cn.academy.block.block.block-generic-ore
  (:require [cn.academy.protocols.block :as block-api])
  (:import [java.util Random]))

;; Ore varieties
(def ore-types
  {:crystal {:name "crystal_ore"
             :harvest-level 2
             :hardness 3.0
             :resistance 5.0
             :light-level 4}
   :imag-crystal {:name "imag_crystal_ore"
                  :harvest-level 2
                  :hardness 3.5
                  :resistance 5.0
                  :light-level 8}})

(defn create-generic-ore [ore-type]
  (let [ore-data (get ore-types ore-type)
        material (-> @block-api/*forge-factory*
                    block-api/create-block-properties
                    (block-api/get-block-material "rock"))
        container (-> @block-api/*forge-factory*
                    (block-api/create-block-container material))]
    
    ;; Set basic block properties
    (block-api/set-hardness! container (:hardness ore-data))
    (block-api/set-harvest-level! container "pickaxe" (:harvest-level ore-data))
    
    container))

;; Factory functions to create ore block instances
(defn create-crystal-ore []
  (create-generic-ore :crystal))

(defn create-imag-crystal-ore []
  (create-generic-ore :imag-crystal))

;; Export the constructor functions for Java interop
(gen-class
  :name cn.academy.block.block.BlockGenericOre$Factory
  :methods [^:static [createCrystalOre [] Object]
            ^:static [createImagCrystalOre [] Object]]
  :prefix "block-factory-")

(defn block-factory-createCrystalOre []
  (create-crystal-ore))

(defn block-factory-createImagCrystalOre []
  (create-imag-crystal-ore))