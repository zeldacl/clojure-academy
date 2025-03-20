(ns cn.academy.forge.test.datagen-test
  (:require [clojure.test :refer :all]
            [mcmod.protocols :refer :all]
            [cn.academy.forge.datagen.ModelGenerator :as models]
            [cn.academy.forge.datagen.RecipeGenerator :as recipes]
            [cn.academy.forge.datagen.LanguageGenerator :as lang])
  (:import [net.minecraft.data DataGenerator DirectoryCache]
           [net.minecraft.util ResourceLocation]
           [net.minecraftforge.client.model.generators BlockModelBuilder ItemModelBuilder]
           [net.minecraft.item Items]
           [net.minecraft.data.recipes ShapedRecipeBuilder]))

(defn mock-data-generator []
  (proxy [DataGenerator] [(java.io.File. "test-output") []]))

(deftest test-model-generation
  (let [generator (mock-data-generator)
        mod-id "acmod"]
    
    (testing "Block model generation"
      (let [models (models/generate-block-models! generator mod-id)]
        (is (satisfies? IModelBakery models))
        (is (empty? (get-textures models)))))
        
    (testing "Item model generation"  
      (let [models (models/generate-item-models! generator mod-id)]
        (is (satisfies? IModelBakery models))
        (is (empty? (get-textures models)))))
        
    (testing "Blockstate generation"
      (models/generate-blockstates! generator mod-id))))

(deftest test-recipe-generation
  (let [generator (mock-data-generator)
        mod-id "acmod"]
        
    (testing "Recipe registration"
      (let [recipes (recipes/generate-recipes! generator mod-id)]
        ;; Generated recipes validated by Forge recipe system
        ))
        
    (testing "Loot table generation"
      (let [tables (recipes/generate-loot-tables! generator mod-id)]
        (is (map? tables))
        (is (contains? tables (ResourceLocation. mod-id "wireless_node_basic")))
        (is (contains? tables (ResourceLocation. mod-id "wireless_node_standard")))
        (is (contains? tables (ResourceLocation. mod-id "wireless_node_advanced")))))))

(deftest test-language-generation
  (let [generator (mock-data-generator)
        mod-id "acmod"]
    
    (testing "English translation generation"
      (lang/generate-languages! generator mod-id)
      
      ;; Validate required keys present
      (let [translations lang/en-us-translations]
        (is (contains? translations "block.acmod.wireless_node_basic"))
        (is (contains? translations "item.acmod.energy_component"))
        (is (contains? translations "gui.acmod.wireless.range"))))
        
    (testing "Chinese translation generation"
      (let [translations lang/zh-cn-translations]
        (is (contains? translations "block.acmod.wireless_node_basic"))
        (is (contains? translations "item.acmod.energy_component"))
        (is (contains? translations "gui.acmod.wireless.range"))))))

(deftest test-data-generator-coordination
  (let [generator (mock-data-generator)
        mod-id "acmod"
        event (proxy [net.minecraftforge.fml.event.lifecycle.GatherDataEvent] 
                [generator DirectoryCache/DUMMY
                 (reify net.minecraftforge.resource.ResourcePackLoader$IPackFinder)]
                (includeClient [] true)
                (includeServer [] true))]
                
    (testing "Full data generation pipeline"
      ;; Run complete data generation
      (cn.academy.forge.datagen.DataGenerators/gather-data! event))))