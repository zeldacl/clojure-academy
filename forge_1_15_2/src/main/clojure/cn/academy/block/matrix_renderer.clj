(ns cn.academy.block.matrix-renderer
  (:require [cn.academy.block.matrix-render-core :as render-core]
            [cn.academy.block.matrix-position :as position])
  (:import [net.minecraft.client.renderer.tileentity TileEntityRenderer]
           [com.mojang.blaze3d.matrix MatrixStack]
           [com.mojang.blaze3d.vertex IVertexBuilder]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.client.renderer.model ModelRenderer]))

(def ^:private matrix-texture (ResourceLocation. "academy:textures/blocks/matrix.png"))

(defprotocol IForgeMatrixRenderer
  (render [this tile matrix-stack buffer-in combined-light combined-overlay])
  (apply-transform [this matrix-stack transform])
  (render-model-part [this matrix-stack part light overlay]))

(defrecord ForgeMatrixRenderer [renderer pos-adapter]
  IForgeMatrixRenderer
  (render [_ tile matrix-stack buffer combined-light combined-overlay]
    (let [pos (position/from-block-pos pos-adapter (.getPos tile))
          partial-ticks (.getPartialTickTime Minecraft/getInstance)]
      (render-core/render-tile renderer pos partial-ticks)
      (doseq [render-info (render-core/get-render-queue renderer)]
        (let [{:keys [model part transform]} render-info]
          (.bindTexture TextureManager matrix-texture)
          (when transform
            (apply-transform matrix-stack transform))
          (render-model-part matrix-stack part combined-light combined-overlay)))))
  
  (apply-transform [_ matrix-stack {:keys [translate rotate scale]}]
    (when translate
      (.translate matrix-stack (nth translate 0) (nth translate 1) (nth translate 2)))
    (when rotate
      (doto matrix-stack
        (.rotate (nth rotate 1) [0 1 0])
        (.rotate (nth rotate 0) [1 0 0])
        (.rotate (nth rotate 2) [0 0 1])))
    (when scale
      (.scale matrix-stack (nth scale 0) (nth scale 1) (nth scale 2))))
  
  (render-model-part [_ matrix-stack part light overlay]
    (let [buffer (-> (Tessellator/getInstance)
                    .getBuilder)]
      (.render part matrix-stack buffer light overlay))))

(defn create-renderer [matrix model]
  (->ForgeMatrixRenderer
    (render-core/create-renderer matrix model)
    (position/create-adapter)))