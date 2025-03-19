(ns cn.li.bridge.matrix.render
  (:require [cn.li.bridge.matrix.api :as api]
            [cn.li.bridge.render.api :as render])
  (:import [net.minecraft.client.renderer.tileentity TileEntityRenderer]
           [com.mojang.blaze3d.matrix MatrixStack]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.client.renderer IRenderTypeBuffer]
           [net.minecraft.util.math.vector Vector3f]))

(def ^:private matrix-model
  (ResourceLocation. "li:models/block/matrix.obj"))

(def ^:private matrix-texture
  (ResourceLocation. "li:textures/block/matrix.png"))

(def ^:private plate-positions
  [[0.0 0.25 -0.5]  ; North
   [0.5 0.25 0.0]   ; East  
   [0.0 0.25 0.5]]) ; South

(defrecord MatrixRenderer [matrix]
  render/IRenderer
  (render [_ pos buffer-source render-data]
    (let [{:keys [matrix-stack partial-ticks light overlay]} render-data
          state (api/sync-with-client matrix)
          {:keys [formed? core-level plates]} (:state state)]
      
      ;; Save matrix state
      (.pushPose matrix-stack)
      
      ;; Translate to center
      (.translate matrix-stack 0.5 0.0 0.5)
      
      ;; Render base model
      (render/bind-texture matrix-texture)
      (render/render-model matrix-model matrix-stack buffer-source light overlay)
      
      ;; Render plates if formed
      (when formed?
        (doseq [[idx pos] (map-indexed vector plate-positions)
                :when (get plates idx)]
          (.pushPose matrix-stack)
          (apply #(.translate matrix-stack %1 %2 %3) pos)
          (.mulPose matrix-stack (Vector3f/YP (* 90 idx)))
          (render/render-model matrix-model matrix-stack buffer-source light overlay)
          (.popPose matrix-stack)))
      
      ;; Render core effects
      (when (and formed? (pos? core-level))
        (let [time (/ (System/currentTimeMillis) 1000.0)
              height (* 0.1 (Math/sin (* 2 Math/PI time)))]
          (.translate matrix-stack 0.0 height 0.0)
          (render/render-model matrix-model matrix-stack buffer-source light overlay)))
      
      ;; Restore matrix state
      (.popPose matrix-stack)))
  
  (get-render-layer [_]
    (render/get-translucent-render-type)))

(defn create-renderer [matrix]
  (->MatrixRenderer matrix))

(defn register-renderer [registry]
  (render/register-renderer registry "matrix_renderer" create-renderer))