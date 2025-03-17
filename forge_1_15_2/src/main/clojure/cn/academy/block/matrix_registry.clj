(ns cn.academy.block.matrix-registry
  (:require [cn.academy.block.matrix-forge :as matrix-forge]
            [cn.academy.block.matrix-model :as matrix-model])
  (:import [net.minecraftforge.fml.common Mod$EventBusSubscriber]
           [net.minecraftforge.event RegistryEvent]
           [net.minecraftforge.fml.client.registry ClientRegistry]
           [net.minecraft.block Block]
           [net.minecraft.item Item]))

(defprotocol IMatrixRegistry
  (register-block [this registry])
  (register-tile-entity [this])
  (register-model [this])
  (register-renderers [this]))

(defrecord MatrixRegistryHandler [mod-id resource-manager]
  IMatrixRegistry
  (register-block [_ registry]
    (let [matrix (matrix-forge/create-forge-matrix)]
      (.register registry
        (-> matrix
            (.setRegistryName (str mod-id ":matrix"))))))
  
  (register-tile-entity [_]
    (let [tile-type (matrix-forge/create-tile-type)]
      (ClientRegistry/registerTileEntity 
        tile-type
        (str mod-id ":matrix_tile"))))
  
  (register-model [_]
    (let [loader (matrix-model/create-matrix-model-loader resource-manager)]
      (-> (ModelLoadingRegistry/get)
          (.registerLoader 
            (ResourceLocation. mod-id "matrix_model")
            loader))))
  
  (register-renderers [_]
    (ClientRegistry/bindTileEntityRenderer
      matrix-forge/MatrixTileType
      (matrix-forge/create-tile-renderer resource-manager))))