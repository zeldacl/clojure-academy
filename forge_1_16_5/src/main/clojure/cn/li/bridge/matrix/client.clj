(ns cn.li.bridge.matrix.client
  (:require [cn.li.bridge.matrix.api :as matrix])
  (:import [net.minecraft.client.renderer.texture TextureAtlasSprite]
           [net.minecraft.client.renderer RenderType]
           [net.minecraft.util ResourceLocation]))

(def ^:private matrix-texture 
  (ResourceLocation. "academy" "textures/blocks/matrix.png"))

(defprotocol IMatrixRenderer
  (render-matrix [this matrix pos partials])
  (get-energy-fill [this matrix])
  (get-form-progress [this matrix]))

(defrecord MatrixClientHandler [renderer]
  IMatrixRenderer
  (render-matrix [_ matrix pos partials]
    (when (matrix/is-formed? matrix)
      (let [energy-fill (get-energy-fill matrix)
            form-progress (get-form-progress matrix)]
        (.renderMatrix renderer pos energy-fill form-progress))))
  
  (get-energy-fill [_ matrix]
    (let [energy (matrix/get-energy-stored matrix)
          capacity (matrix/get-energy-capacity matrix)]
      (if (pos? capacity)
        (/ energy capacity)
        0.0)))
  
  (get-form-progress [_ matrix]
    (if (matrix/is-formed? matrix)
      1.0
      0.0)))

(defn create-client-handler [renderer]
  (->MatrixClientHandler renderer))

(defn register-client-renderer [registry]
  ;; Register custom renderer implementation
  (.register registry "matrix" 
    (fn []
      (proxy [net.minecraft.client.renderer.tileentity.TileEntityRenderer] []
        (func_225616_a_ [matrix-tile pos state partials buf light overlay]
          (when-let [matrix (.getMatrix matrix-tile)]
            (render-matrix matrix pos partials)))))))