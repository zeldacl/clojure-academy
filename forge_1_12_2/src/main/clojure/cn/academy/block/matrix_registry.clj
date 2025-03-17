(ns cn.academy.block.matrix-registry
  (:require [cn.academy.block.matrix-forge :as matrix-forge]
            [cn.academy.block.matrix-model :as matrix-model])
  (:import [net.minecraftforge.fml.common.registry GameRegistry]
           [net.minecraftforge.client.model ModelLoader]
           [net.minecraft.block Block]
           [net.minecraft.item Item]))

(defprotocol IMatrixRegistry
  (register-block [this])
  (register-tile-entity [this])
  (register-models [this])
  (register-renderers [this]))

(defrecord MatrixRegistryHandler [mod-id resource-manager]
  IMatrixRegistry
  (register-block [_]
    (let [matrix (matrix-forge/create-forge-matrix)
          block-name "matrix"]
      (GameRegistry/register matrix (str mod-id ":" block-name))
      (GameRegistry/register 
        (matrix-forge/create-item-block matrix)
        (.getRegistryName matrix))))
  
  (register-tile-entity [_]
    (GameRegistry/registerTileEntity
      (matrix-forge/get-tile-class)
      (str mod-id ":matrix_tile")))
  
  (register-models [_]
    (let [model-loc (ResourceLocation. mod-id "matrix")
          loader (matrix-model/create-matrix-model-loader resource-manager)]
      (ModelLoader/setCustomModelResourceLocation
        (matrix-forge/get-matrix-item)
        0
        (ModelResourceLocation. model-loc "inventory"))))
  
  (register-renderers [_]
    (ClientRegistry/bindTileEntitySpecialRenderer
      (matrix-forge/get-tile-class)
      (matrix-forge/create-tile-renderer resource-manager))))