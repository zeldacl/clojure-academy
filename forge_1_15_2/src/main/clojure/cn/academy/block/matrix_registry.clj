(ns cn.academy.block.matrix-registry
  (:require [cn.academy.block.block.block-matrix :as matrix]
            [cn.academy.block.registration-impl :as reg-impl]
            [cn.academy.block.matrix-model :as matrix-model]
            [cn.academy.block.material-impl :as material])
  (:import [net.minecraftforge.fml.common Mod$EventBusSubscriber]
           [net.minecraftforge.event RegistryEvent]
           [net.minecraftforge.fml.client.registry ClientRegistry]
           [net.minecraft.block Block Block$Properties]
           [net.minecraft.block.material Material]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.item Item]))

(defprotocol IMatrixRegistry
  (register-block [this registry])
  (register-tile-entity [this])
  (register-model [this])
  (register-renderers [this]))

(defrecord MatrixRegistryHandler [mod-id resource-manager]
  IMatrixRegistry
  (register-block [_ registry]
    (let [matrix-def (matrix-core/create-matrix)
          block (create-forge-block matrix-def)]
      (.register registry
        (doto block
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

(defn create-forge-block [matrix-def]
  (let [props (:properties matrix-def)
        handlers (:event-handlers matrix-def)
        material-mapper (material/create-material-mapper)
        block-props (-> (Block$Properties/create (.get-material material-mapper (:material props)))
                       (.hardnessAndResistance (:hardness props))
                       (.lightValue (:light-level props)))]
    (proxy [Block] [block-props]
      (onBlockActivated [state world pos player hand direction hit]
        ((:on-block-activated handlers) world pos state player))
      (onBlockPlacedBy [world pos state placer stack]
        ((:on-block-placed handlers) world pos state placer stack)))))

(defn register-matrix [mod-id]
  (let [registration (reg-impl/create-registration)]
    (matrix/register! registration mod-id)))