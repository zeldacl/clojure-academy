(ns cn.academy.forge.datagen.ModelGenerator
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core])
  (:import [net.minecraft.data DataGenerator]
           [net.minecraft.data.models.model ModelTextures]
           [net.minecraft.data.models.blockstates MultiPartBlockStateBuilder]
           [net.minecraftforge.client.model.generators BlockModelBuilder ItemModelBuilder]))

(defrecord ForgeModelGenerator [^DataGenerator generator mod-id existing-models]
  IModelBakery
  (bake-model [this state format sprite-getter]
    (let [model (get @existing-models state)]
      (.bake model format sprite-getter)))
      
  (get-textures [this]
    (mapcat #(.getTextures %) (vals @existing-models)))
    
  (get-overrides [this]
    []))

(defn generate-block-models! [generator mod-id]
  (let [blocks-dir (str "assets/" mod-id "/models/block")
        model-gen (->ForgeModelGenerator generator mod-id (atom {}))]
    
    ;; Generate wireless node models
    (doto (BlockModelBuilder. blocks-dir "wireless_node_basic")
      (.parent (ResourceLocation. "block/cube_all"))
      (.texture "all" (str mod-id ":block/wireless_node_basic")))
      
    (doto (BlockModelBuilder. blocks-dir "wireless_node_standard") 
      (.parent (ResourceLocation. "block/cube_all"))
      (.texture "all" (str mod-id ":block/wireless_node_standard")))
      
    (doto (BlockModelBuilder. blocks-dir "wireless_node_advanced")
      (.parent (ResourceLocation. "block/cube_all")) 
      (.texture "all" (str mod-id ":block/wireless_node_advanced")))
      
    ;; Generate machine block models
    (doto (BlockModelBuilder. blocks-dir "machine_block")
      (.parent (ResourceLocation. "block/cube"))
      (.texture "north" (str mod-id ":block/machine_front"))
      (.texture "south" (str mod-id ":block/machine_side"))
      (.texture "east" (str mod-id ":block/machine_side")) 
      (.texture "west" (str mod-id ":block/machine_side"))
      (.texture "up" (str mod-id ":block/machine_top"))
      (.texture "down" (str mod-id ":block/machine_bottom")))
      
    model-gen))

(defn generate-item-models! [generator mod-id]
  (let [items-dir (str "assets/" mod-id "/models/item")
        model-gen (->ForgeModelGenerator generator mod-id (atom {}))]
        
    ;; Generate wireless node item models  
    (doto (ItemModelBuilder. items-dir "wireless_node_basic")
      (.parent (ResourceLocation. mod-id "block/wireless_node_basic")))
      
    (doto (ItemModelBuilder. items-dir "wireless_node_standard")
      (.parent (ResourceLocation. mod-id "block/wireless_node_standard")))
      
    (doto (ItemModelBuilder. items-dir "wireless_node_advanced") 
      (.parent (ResourceLocation. mod-id "block/wireless_node_advanced")))
      
    ;; Generate component item models
    (doto (ItemModelBuilder. items-dir "energy_component")
      (.parent (ResourceLocation. "item/generated"))
      (.texture "layer0" (str mod-id ":item/energy_component")))
      
    (doto (ItemModelBuilder. items-dir "wireless_component")
      (.parent (ResourceLocation. "item/generated"))
      (.texture "layer0" (str mod-id ":item/wireless_component")))
      
    model-gen))

(defn generate-blockstates! [generator mod-id]
  (let [blockstates-dir (str "assets/" mod-id "/blockstates")]
    
    ;; Generate wireless node blockstates
    (doto (MultiPartBlockStateBuilder. 
            (ResourceLocation. mod-id "wireless_node_basic"))
      (.multipart)
      (.with (ModelTextures/getBlockTextures)))
      
    (doto (MultiPartBlockStateBuilder.
            (ResourceLocation. mod-id "wireless_node_standard")) 
      (.multipart)
      (.with (ModelTextures/getBlockTextures)))
      
    (doto (MultiPartBlockStateBuilder.
            (ResourceLocation. mod-id "wireless_node_advanced"))
      (.multipart) 
      (.with (ModelTextures/getBlockTextures)))))

(defn run-generators! [^DataGenerator generator mod-id]
  (let [block-models (generate-block-models! generator mod-id)
        item-models (generate-item-models! generator mod-id)]
    (generate-blockstates! generator mod-id)))