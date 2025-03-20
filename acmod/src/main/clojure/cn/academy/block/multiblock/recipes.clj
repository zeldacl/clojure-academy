(ns cn.academy.block.multiblock.recipes
  (:require [cn.academy.block.multiblock.machine-state :as machine])
  (:import [net.minecraft.item ItemStack]))

(defprotocol IRecipeHandler
  (can-process? [this recipe] "Check if recipe can be processed")
  (start-recipe! [this recipe] "Start processing a recipe")
  (process-tick! [this] "Process current recipe for one tick")
  (finish-recipe! [this] "Complete current recipe processing")
  (get-progress [this] "Get current recipe progress"))

(defrecord Recipe [inputs outputs process-time energy-per-tick]
  Object
  (toString [_]
    (str "Recipe[inputs=" inputs 
         ", outputs=" outputs 
         ", time=" process-time 
         ", energy=" energy-per-tick "]")))

(defrecord MultiblockRecipeHandler [machine state-atom inventory]
  IRecipeHandler
  (can-process? [_ recipe]
    (and (machine/has-energy? machine 
                             (* (:process-time recipe)
                                (:energy-per-tick recipe)))
         (every? (fn [[slot required]]
                  (let [actual (.getStackInSlot inventory slot)]
                    (and (= (.getItem required) (.getItem actual))
                         (>= (.getCount actual) (.getCount required)))))
                 (:inputs recipe))
         (every? (fn [[slot output]]
                  (let [actual (.getStackInSlot inventory slot)]
                    (or (.isEmpty actual)
                        (and (= (.getItem output) (.getItem actual))
                             (<= (+ (.getCount actual) 
                                   (.getCount output))
                                 (.getMaxStackSize actual))))))
                 (:outputs recipe))))
  
  (start-recipe! [this recipe]
    (when (can-process? this recipe)
      (doseq [[slot input] (:inputs recipe)]
        (let [stack (.getStackInSlot inventory slot)]
          (.shrink stack (.getCount input))))
      (reset! state-atom
              {:current-recipe recipe
               :progress 0})))
  
  (process-tick! [this]
    (when-let [recipe (:current-recipe @state-atom)]
      (when (and (can-process? this recipe)
                 (machine/use-energy! machine 
                                    (:energy-per-tick recipe)))
        (swap! state-atom update :progress inc)
        (when (= (:progress @state-atom) 
                 (:process-time recipe))
          (finish-recipe! this)))))
  
  (finish-recipe! [_]
    (when-let [recipe (:current-recipe @state-atom)]
      (doseq [[slot output] (:outputs recipe)]
        (let [stack (.getStackInSlot inventory slot)]
          (if (.isEmpty stack)
            (.setStackInSlot inventory slot (.copy output))
            (.grow stack (.getCount output)))))
      (reset! state-atom nil)))
  
  (get-progress [_]
    (when-let [recipe (:current-recipe @state-atom)]
      (/ (:progress @state-atom)
         (:process-time recipe)))))

(defn create-recipe-handler
  "Create a new recipe handler for a multiblock machine"
  [machine inventory]
  (->MultiblockRecipeHandler machine (atom nil) inventory))