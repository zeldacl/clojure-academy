(ns cn.academy.forge.datagen.RecipeGenerator
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core])
  (:import [net.minecraft.data DataGenerator]
           [net.minecraft.data.recipes ShapedRecipeBuilder ShapelessRecipeBuilder]
           [net.minecraft.item Items]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.loot LootTable LootPool]
           [net.minecraft.loot.functions SetCount]))

(defn register-wireless-recipes! [consumer mod-id]
  ;; Basic wireless node
  (-> (ShapedRecipeBuilder/shaped (get-registry-object mod-id "wireless_node_basic"))
      (.pattern "iri")
      (.pattern "rdr")
      (.pattern "iri")
      (.define \i Items/IRON_INGOT)
      (.define \r Items/REDSTONE)
      (.define \d Items/DIAMOND)
      (.unlockedBy "has_diamond" (has-item-trigger Items/DIAMOND))
      (.save consumer (ResourceLocation. mod-id "wireless_node_basic")))
      
  ;; Standard wireless node
  (-> (ShapedRecipeBuilder/shaped (get-registry-object mod-id "wireless_node_standard"))
      (.pattern "grg")
      (.pattern "rnr")
      (.pattern "grg")
      (.define \g Items/GOLD_INGOT)
      (.define \r (get-registry-object mod-id "wireless_component"))
      (.define \n (get-registry-object mod-id "wireless_node_basic"))
      (.unlockedBy "has_basic_node" (has-item-trigger (get-registry-object mod-id "wireless_node_basic")))
      (.save consumer (ResourceLocation. mod-id "wireless_node_standard")))
      
  ;; Advanced wireless node  
  (-> (ShapedRecipeBuilder/shaped (get-registry-object mod-id "wireless_node_advanced"))
      (.pattern "ere")
      (.pattern "rnr")
      (.pattern "ere")
      (.define \e Items/EMERALD)
      (.define \r (get-registry-object mod-id "wireless_component"))
      (.define \n (get-registry-object mod-id "wireless_node_standard"))
      (.unlockedBy "has_standard_node" (has-item-trigger (get-registry-object mod-id "wireless_node_standard")))
      (.save consumer (ResourceLocation. mod-id "wireless_node_advanced"))))

(defn register-component-recipes! [consumer mod-id]
  ;; Energy component
  (-> (ShapelessRecipeBuilder/shapeless (get-registry-object mod-id "energy_component"))
      (.requires Items/REDSTONE)
      (.requires Items/GOLD_INGOT)
      (.requires Items/QUARTZ)
      (.unlockedBy "has_redstone" (has-item-trigger Items/REDSTONE))
      (.save consumer (ResourceLocation. mod-id "energy_component")))
      
  ;; Wireless component
  (-> (ShapedRecipeBuilder/shaped (get-registry-object mod-id "wireless_component"))
      (.pattern " r ")
      (.pattern "rer")
      (.pattern " r ")
      (.define \r Items/REDSTONE)
      (.define \e (get-registry-object mod-id "energy_component"))
      (.unlockedBy "has_energy_component" (has-item-trigger (get-registry-object mod-id "energy_component")))
      (.save consumer (ResourceLocation. mod-id "wireless_component"))))

(defn generate-block-loot! [tables mod-id]
  ;; Wireless nodes drop themselves
  (doto tables
    (.put (ResourceLocation. mod-id "wireless_node_basic")
          (-> (LootTable/lootTable)
              (.withPool (-> (LootPool/lootPool)
                            (.add (ItemLootEntry/lootTableItem 
                                   (get-registry-object mod-id "wireless_node_basic")))
                            (.when (SurvivesExplosion/survivesExplosion))))))
              
    (.put (ResourceLocation. mod-id "wireless_node_standard")
          (-> (LootTable/lootTable)
              (.withPool (-> (LootPool/lootPool)
                            (.add (ItemLootEntry/lootTableItem
                                   (get-registry-object mod-id "wireless_node_standard")))
                            (.when (SurvivesExplosion/survivesExplosion))))))
                            
    (.put (ResourceLocation. mod-id "wireless_node_advanced")
          (-> (LootTable/lootTable)
              (.withPool (-> (LootPool/lootPool)
                            (.add (ItemLootEntry/lootTableItem
                                   (get-registry-object mod-id "wireless_node_advanced")))
                            (.when (SurvivesExplosion/survivesExplosion))))))))

;; Main generator functions
(defn generate-recipes! [^DataGenerator generator mod-id]
  (let [consumer (.accept generator)]
    (register-wireless-recipes! consumer mod-id)
    (register-component-recipes! consumer mod-id)))
    
(defn generate-loot-tables! [^DataGenerator generator mod-id]
  (let [tables (atom {})]
    (generate-block-loot! tables mod-id)
    @tables))