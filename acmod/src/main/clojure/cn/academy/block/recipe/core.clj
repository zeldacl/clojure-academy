(ns cn.academy.block.recipe.core
  (:require [clojure.tools.logging :as log]))

;; Recipe registry
(def recipe-registry (atom {}))

(defn register-recipe! [machine-type recipe]
  (swap! recipe-registry update machine-type
         (fn [recipes] (conj (or recipes #{}) recipe))))

;; Recipe validation and matching
(defn matches-input? [recipe inputs]
  (every? (fn [[slot-id required]]
            (when-let [input (get inputs slot-id)]
              (mcmod.item/matches? input required)))
          (:inputs recipe)))

(defn find-matching-recipe [machine-type inputs]
  (when-let [recipes (get @recipe-registry machine-type)]
    (first (filter #(matches-input? % inputs) recipes))))

;; Recipe processing
(defn can-process? [block recipe]
  (and (>= (get-in block [:state :energy]) 
           (get recipe :energy-cost 0))
       (let [inventory (mcmod.capability/get-capability block :inventory nil)]
         (every? (fn [[slot amount]]
                  (mcmod.inventory/can-extract? inventory slot amount))
                (:outputs recipe)))))

(defn process-recipe! [block recipe]
  (let [inventory (mcmod.capability/get-capability block :inventory nil)]
    ;; Extract inputs
    (doseq [[slot amount] (:inputs recipe)]
      (mcmod.inventory/extract! inventory slot amount))
    
    ;; Add outputs
    (doseq [[slot item] (:outputs recipe)]
      (mcmod.inventory/insert! inventory slot item))
    
    ;; Consume energy
    (swap! (:state block) update :energy - (:energy-cost recipe))))

;; Default recipes
(def machine-recipes
  {:metal-former
   #{{:inputs {"input" {:item "minecraft:iron_ingot" :count 1}}
      :outputs {"output" {:item "academy:plate_iron" :count 1}}
      :energy-cost 1000
      :process-time 100}
     {:inputs {"input" {:item "minecraft:gold_ingot" :count 1}}
      :outputs {"output" {:item "academy:plate_gold" :count 1}}
      :energy-cost 1000
      :process-time 100}}
   
   :imag-fusor
   #{{:inputs {"input1" {:item "academy:imaginary_matter" :count 1}
               "input2" {:item "minecraft:diamond" :count 1}}
      :outputs {"output" {:item "academy:imaginary_crystal" :count 1}}
      :energy-cost 5000
      :process-time 200}}})

;; Initialize recipe system
(defn init-recipes! []
  (doseq [[machine-type recipes] machine-recipes]
    (doseq [recipe recipes]
      (register-recipe! machine-type recipe)))
  true)