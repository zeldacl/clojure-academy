(ns cljacademy.forge.blocks.matrix-registry
  (:require [cljacademy.blocks.matrix :as matrix]
            [cljacademy.forge.blocks.matrix-model :as model]
            [cljacademy.forge.blocks.matrix-render :as render])
  (:import [net.minecraft.block Block]
           [net.minecraft.item ItemBlock]
           [net.minecraft.util ResourceLocation]
           [net.minecraftforge.fml.common.registry GameRegistry]
           [net.minecraftforge.fml.client.registry ClientRegistry]
           [net.minecraftforge.client.model ModelLoader]))

(defn register-block []
  (let [block (matrix/create-matrix-block)
        block-id "wireless_matrix"
        registry-name (ResourceLocation. "cljacademy" block-id)]
    (.setRegistryName block registry-name)
    (GameRegistry/register block)
    
    (let [item (ItemBlock. block)]
      (.setRegistryName item registry-name)
      (GameRegistry/register item))
    
    block))

(defn register-tile-entity [block]
  (GameRegistry/registerTileEntity 
    (matrix/get-tile-entity-class)
    (ResourceLocation. "cljacademy" "wireless_matrix")))

(defn register-client-components []
  (let [model-loader (model/create-model-loader)]
    (ModelLoader/setCustomModelResourceLocation 
      matrix/MATRIX_ITEM 0 
      (ModelResourceLocation. "cljacademy:wireless_matrix" "inventory"))
    (ClientRegistry/bindTileEntitySpecialRenderer 
      matrix/MatrixTileEntity
      (render/create-tile-renderer))))