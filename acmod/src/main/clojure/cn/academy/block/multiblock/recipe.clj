(ns cn.academy.block.multiblock.recipe
  (:require [clojure.spec.alpha :as s])
  (:import [net.minecraft.item ItemStack]))

(defprotocol IMultiblockRecipe
  (get-inputs [this] "Get input items required")
  (get-outputs [this] "Get output items produced")
  (get-process-time [this] "Get processing time in ticks")
  (get-energy-cost [this] "Get energy cost per tick"))

(defrecord MultiblockRecipe [inputs outputs process-time energy-cost]
  IMultiblockRecipe
  (get-inputs [_] inputs)
  (get-outputs [_] outputs) 
  (get-process-time [_] process-time)
  (get-energy-cost [_] energy-cost))

(def recipes (atom {}))

(defn register-recipe!
  "Register a new multiblock machine recipe"
  [machine-type {:keys [inputs outputs process-time energy-cost] :as recipe}]
  (swap! recipes update machine-type conj 
         (map->MultiblockRecipe recipe)))

(defn find-recipe
  "Find first matching recipe for given machine type and inputs"
  [machine-type input-stacks]
  (let [machine-recipes (get @recipes machine-type)]
    (first
     (filter (fn [recipe]
              (every? (fn [[item amount]]
                       (let [found (first (filter #(ItemStack/areItemsEqual item %) 
                                                input-stacks))]
                         (and found (>= (.getCount found) amount))))
                     (:inputs recipe)))
            machine-recipes))))

(defn consume-inputs!
  "Consume input items for recipe from inventory"
  [recipe inventory]
  (doseq [[item amount] (:inputs recipe)]
    (let [slot (.findSlotWithItem inventory item)]
      (.decrStackSize inventory slot amount))))

(defn add-outputs!
  "Add output items from recipe to inventory" 
  [recipe inventory]
  (doseq [stack (:outputs recipe)]
    (.addItem inventory (.copy stack))))