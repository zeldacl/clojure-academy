(ns cn.academy.block.multiblock.recipes
  (:require [cn.academy.block.multiblock.machine-state :as machine]
            [mcmod.item :as item]
            [mcmod.inventory :as inv]))

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
                  (let [actual (inv/get-stack-in-slot inventory slot)]
                    (and (= (item/get-item required) (item/get-item actual))
                         (>= (item/get-count actual) (item/get-count required)))))
                 (:inputs recipe))
         (every? (fn [[slot output]]
                  (let [actual (inv/get-stack-in-slot inventory slot)]
                    (or (item/is-empty? actual)
                        (and (= (item/get-item output) (item/get-item actual))
                             (<= (+ (item/get-count actual) 
                                   (item/get-count output))
                                 (item/get-max-stack-size actual))))))
                 (:outputs recipe))))
  
  (start-recipe! [this recipe]
    (when (can-process? this recipe)
      (doseq [[slot input] (:inputs recipe)]
        (let [stack (inv/get-stack-in-slot inventory slot)]
          (item/shrink stack (item/get-count input))))
      (reset! state-atom
              {:current-recipe recipe
               :progress 0})))
  
  (process-tick! [this]
    (when-let [recipe (:current-recipe @state-atom)]
      (when (can-process? this recipe)
        (if (machine/consume-energy! machine (:energy-per-tick recipe))
          (let [progress (inc (:progress @state-atom))]
            (if (>= progress (:process-time recipe))
              (do (finish-recipe! this)
                  (reset! state-atom nil))
              (swap! state-atom assoc :progress progress)))))))
  
  (finish-recipe! [_ recipe]
    (doseq [[slot output] (:outputs recipe)]
      (let [stack (inv/get-stack-in-slot inventory slot)]
        (if (item/is-empty? stack)
          (inv/set-stack-in-slot inventory slot (item/copy output))
          (item/grow stack (item/get-count output))))))
  
  (get-progress [_]
    (get-in @state-atom [:progress] 0)))

(defn create-handler
  "Create a new recipe handler for a multiblock machine"
  [machine inventory]
  (->MultiblockRecipeHandler machine (atom nil) inventory))

(defprotocol IRecipeRegistry
  (register-recipe! [this recipe] "Register a new recipe")
  (get-recipes [this] "Get all registered recipes")
  (find-recipe [this inputs] "Find matching recipe for inputs"))

(defrecord RecipeRegistry [recipes-atom]
  IRecipeRegistry
  (register-recipe! [_ recipe]
    (swap! recipes-atom conj recipe))
  
  (get-recipes [_]
    @recipes-atom)
  
  (find-recipe [_ inputs]
    (first
     (filter (fn [{recipe-inputs :inputs}]
               (every? (fn [[slot stack]]
                        (when-let [required (get recipe-inputs slot)]
                          (and (= (item/get-item required) (item/get-item stack))
                               (>= (item/get-count stack) 
                                   (item/get-count required)))))
                     inputs))
            @recipes-atom))))

(defn create-registry
  "Create a new recipe registry"
  []
  (->RecipeRegistry (atom [])))