(ns cn.academy.block.matrix-registry
  (:require [cn.academy.block.matrix-forge :as matrix-forge]
            [cn.academy.block.matrix-model :as matrix-model])
  (:import [net.minecraftforge.fml.common Mod$EventBusSubscriber]
           [net.minecraftforge.event RegistryEvent$Register]
           [net.minecraftforge.fml.client.registry ClientRegistry]
           [net.minecraft.block Block]
           [net.minecraft.item BlockItem]))

(defprotocol IMatrixRegistry
  (register-block [this])
  (register-tile-entity [this])
  (register-model-loader [this])
  (register-renderers [this]))

(defrecord MatrixRegistryHandler [mod-id resource-manager]
  IMatrixRegistry
  (register-block [_]
    (let [matrix (matrix-forge/create-forge-matrix)]
      {:block matrix
       :item (-> (BlockItem. (:block matrix) {})
                 (.setRegistryName (str mod-id ":matrix")))}))
  
  (register-tile-entity [_]
    (let [tile-type (matrix-forge/create-tile-type)]
      (ClientRegistry/registerTileEntity 
        tile-type
        (str mod-id ":matrix_tile"))))
  
  (register-model-loader [_]
    (let [loader (matrix-model/create-matrix-model-loader resource-manager)]
      (.register ModelLoaderRegistry 
                (str mod-id ":matrix_model")
                loader)))
  
  (register-renderers [_]
    (ClientRegistry/bindTileEntityRenderer
      matrix-forge/MatrixTileType
      (matrix-forge/create-tile-renderer resource-manager))))