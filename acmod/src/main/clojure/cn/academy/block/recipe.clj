(ns cn.academy.block.recipe
  (:require [mcmod.protocols :refer [IRecipeProcessor IRecipeRegistry]]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Recipe state tracking
(def recipe-state
  (atom {:recipes {}
         :recipe-categories {}
         :active-recipes {}}))

;; Recipe registry implementation
(defrecord RecipeRegistry [state-atom]
  IRecipeRegistry
  (register-recipe! [_ recipe]
    (let [recipe-id (:id recipe)
          category (:category recipe)]
      (swap! state-atom update-in [:recipes category] assoc recipe-id recipe)
      recipe))
  
  (get-recipe [_ recipe-id category]
    (get-in @state-atom [:recipes category recipe-id]))
  
  (get-recipes-by-category [_ category]
    (vals (get-in @state-atom [:recipes category] {})))
  
  (get-all-recipes [_]
    (mapcat vals (vals (:recipes @state-atom))))
  
  (register-category! [_ category properties]
    (swap! state-atom assoc-in [:recipe-categories category] properties))
  
  (get-category-properties [_ category]
    (get-in @state-atom [:recipe-categories category])))

;; Recipe processor implementation  
(defrecord MachineRecipeProcessor [block recipe-registry]
  IRecipeProcessor
  (get-active-recipe [_]
    (get-in @(:state block) [:current-recipe]))
  
  (set-active-recipe! [_ recipe]
    (swap! (:state block) assoc :current-recipe recipe))
  
  (can-process? [_ recipe]
    (when-let [machine (get-in block [:state :machine])]
      (and (.has-resources? machine (get-inputs recipe))
           (.has-space? machine (get-outputs recipe)))))
  
  (start-processing! [this recipe]
    (when (.can-process? this recipe)
      (.set-active-recipe! this recipe)
      (swap! (:state block) assoc :progress 0)))
  
  (finish-processing! [this]
    (when-let [recipe (.get-active-recipe this)]
      (when-let [machine (get-in block [:state :machine])]
        (.consume-resources! machine (get-inputs recipe))
        (.add-resources! machine (get-outputs recipe))
        (.set-active-recipe! this nil)))))

;; Recipe validation and lookup
(defn validate-recipe [recipe]
  (and (:id recipe)
       (:category recipe)
       (sequential? (:inputs recipe))
       (sequential? (:outputs recipe))
       (number? (:process-time recipe))))

(defn find-matching-recipe [block category]
  (when-let [processor (get-in block [:state :processor])]
    (let [recipes (.get-recipes-by-category recipe-registry category)]
      (some #(when (.can-process? processor %) %) recipes))))

;; Recipe factory functions
(defn create-recipe-registry []
  (->RecipeRegistry recipe-state))

(defn create-recipe-processor [block registry]
  (->MachineRecipeProcessor block registry))

;; Recipe data helpers
(defn get-inputs [recipe]
  (:inputs recipe))

(defn get-outputs [recipe]
  (:outputs recipe))

(defn get-process-time [recipe]
  (:process-time recipe))

;; Initialize recipe system
(defn init-recipes! []
  (reset! recipe-state 
          {:recipes {}
           :recipe-categories {}
           :active-recipes {}}))