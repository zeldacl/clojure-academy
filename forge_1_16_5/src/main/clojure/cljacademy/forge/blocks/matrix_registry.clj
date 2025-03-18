(ns cljacademy.forge.blocks.matrix-registry
  (:require [cljacademy.blocks.matrix :as matrix]
            [cljacademy.forge.blocks.matrix-model :as model]
            [cljacademy.forge.blocks.matrix-render :as render])
  (:import [net.minecraft.block Block]
           [net.minecraft.item BlockItem]
           [net.minecraft.util ResourceLocation]
           [net.minecraftforge.registries ForgeRegistries]
           [net.minecraftforge.fml.client.registry ClientRegistry]
           [net.minecraftforge.client.model ModelLoaderRegistry]))

(defn register-block []
  (let [block (matrix/create-matrix-block)
        block-id "wireless_matrix"
        registry-name (ResourceLocation. "cljacademy" block-id)]
    (.setRegistryName block registry-name)
    (.register ForgeRegistries/BLOCKS block)
    
    (let [item (BlockItem. block (Block$Properties/create))]
      (.setRegistryName item registry-name)
      (.register ForgeRegistries/ITEMS item))
    
    block))

(defn register-tile-entity [block]
  (let [tile-type (matrix/create-tile-entity-type block)]
    (.register ForgeRegistries/TILE_ENTITIES 
              (.setRegistryName tile-type (ResourceLocation. "cljacademy" "wireless_matrix")))))

(defn register-client-components []
  (let [model-loader (model/create-model-loader)]
    (ModelLoaderRegistry/registerLoader 
      (ResourceLocation. "cljacademy" "wireless_matrix") 
      model-loader)
    (ClientRegistry/bindTileEntityRenderer 
      matrix/MatrixTileEntityType
      (render/create-tile-renderer))))