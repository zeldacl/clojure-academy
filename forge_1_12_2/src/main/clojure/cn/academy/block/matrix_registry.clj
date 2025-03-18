(ns cn.academy.block.matrix-registry
  (:require [cn.academy.block.block.block-matrix :as matrix-core]
            [cn.academy.block.matrix-model :as matrix-model]
            [cn.academy.block.material-impl :as material]
            [cn.academy.block.registration-impl :as reg-impl])
  (:import [net.minecraftforge.fml.common.registry GameRegistry]
           [net.minecraftforge.client.model ModelLoader]
           [net.minecraft.block Block]
           [net.minecraft.util ResourceLocation]))

(defprotocol IMatrixRegistry
  (register-block [this])
  (register-tile-entity [this])
  (register-models [this])
  (register-renderers [this]))

(defn create-forge-block [matrix-def]
  (let [props (:properties matrix-def)
        handlers (:event-handlers matrix-def)
        material-mapper (material/create-material-mapper)]
    (doto (proxy [Block] [(.get-material material-mapper (:material props))]
            (onBlockActivated [world pos state player hand facing hitX hitY hitZ]
              ((:on-block-activated handlers) world pos state player))
            (onBlockPlacedBy [world pos state placer stack]
              ((:on-block-placed handlers) world pos state placer stack)))
      (.setHardness (:hardness props))
      (.setLightLevel (:light-level props)))))

(defrecord MatrixRegistryHandler [mod-id resource-manager]
  IMatrixRegistry
  (register-block [_]
    (let [matrix-def (matrix-core/create-matrix)
          block (create-forge-block matrix-def)
          block-name "matrix"]
      (GameRegistry/register block (str mod-id ":" block-name))
      (GameRegistry/register 
        (matrix-core/create-item-block block)
        (.getRegistryName block))))
  
  (register-tile-entity [_]
    (GameRegistry/registerTileEntity
      (matrix-core/get-tile-class)
      (str mod-id ":matrix_tile")))
  
  (register-models [_]
    (let [model-loc (ResourceLocation. mod-id "matrix")
          loader (matrix-model/create-matrix-model-loader resource-manager)]
      (ModelLoader/setCustomModelResourceLocation
        (matrix-core/get-matrix-item)
        0
        (ModelResourceLocation. model-loc "inventory"))))
  
  (register-renderers [_]
    (ClientRegistry/bindTileEntitySpecialRenderer
      (matrix-core/get-tile-class)
      (matrix-core/create-tile-renderer resource-manager))))

(defn register-matrix [mod-id]
  (let [registration (reg-impl/create-registration)]
    (matrix/register! registration mod-id)))