(ns cn.academy.forge.datagen.DataGenerators
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core]
            [cn.academy.forge.datagen.ModelGenerator :as models]
            [cn.academy.forge.datagen.RecipeGenerator :as recipes]
            [cn.academy.forge.datagen.LanguageGenerator :as lang])
  (:import [net.minecraft.data DataGenerator HashCache DirectoryCache]
           [net.minecraftforge.fml.event.lifecycle GatherDataEvent]))

(defn gather-data! [^GatherDataEvent event]
  (let [generator (.getGenerator event)
        mod-id core/MOD-ID
        existing-data (.getExistingFileHelper event)]
    
    ;; Only run generators if their respective flags are set
    (when (.includeClient event)
      ;; Client-side data (models, blockstates, etc)
      (models/run-generators! generator mod-id)
      (lang/generate-languages! generator mod-id))
      
    (when (.includeServer event)
      ;; Server-side data (recipes, loot tables, etc)
      (recipes/generate-recipes! generator mod-id)
      (recipes/generate-loot-tables! generator mod-id))))

;; Event handler registration
(defn register-generators! [mod-event-bus]
  (.addListener mod-event-bus
    (reify Consumer
      (accept [this event]
        (when (instance? GatherDataEvent event)
          (gather-data! event))))))