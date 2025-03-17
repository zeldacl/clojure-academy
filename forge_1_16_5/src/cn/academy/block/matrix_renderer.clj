(ns cn.academy.block.matrix-renderer
  (:require [cn.academy.block.matrix-render-core :as render-core]
            [cn.academy.block.matrix-position :as position])
  (:import [net.minecraft.client.renderer.tileentity TileEntityRenderer]
           [com.mojang.blaze3d.matrix MatrixStack]
           [com.mojang.blaze3d.vertex IVertexBuilder]
           [net.minecraft.util ResourceLocation]))

(def ^:private matrix-texture (ResourceLocation. "academy:textures/blocks/matrix.png"))

(defprotocol IForgeMatrixRenderer
  (render [this tile matrix-stack buffer combined-light combined-overlay])
  (apply-transform [this matrix-stack transform])
  (render-model-part [this builder matrix-stack part light overlay]))

(defrecord ForgeMatrixRenderer [renderer pos-adapter]
  IForgeMatrixRenderer
  (render [_ tile matrix-stack buffer combined-light combined-overlay]
    (let [pos (position/from-block-pos pos-adapter (.getBlockPos tile))
          partial-ticks (.getPartialTicks Minecraft/getInstance)]
      (render-core/render-tile renderer pos partial-ticks)
      (doseq [render-info (render-core/get-render-queue renderer)]
        (let [{:keys [model part transform]} render-info]
          (when transform
            (apply-transform matrix-stack transform))
          (render-model-part model buffer matrix-stack part combined-light combined-overlay)))))
  
  (apply-transform [_ matrix-stack {:keys [translate rotate scale]}]
    (when translate
      (.translate matrix-stack (nth translate 0) (nth translate 1) (nth translate 2)))
    (when rotate
      (.mulPose matrix-stack (Quaternion. (nth rotate 0) (nth rotate 1) (nth rotate 2) true)))
    (when scale
      (.scale matrix-stack (nth scale 0) (nth scale 1) (nth scale 2))))
  
  (render-model-part [_ builder matrix-stack part light overlay]
    (.renderToBuffer model part matrix-stack builder light overlay)))

(defn create-renderer [matrix model]
  (->ForgeMatrixRenderer
    (render-core/create-renderer matrix model)
    (position/create-adapter)))