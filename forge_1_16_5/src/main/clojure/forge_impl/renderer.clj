(ns forge-impl.renderer
  (:import [net.minecraft.client.renderer.tileentity TileEntityRenderer TileEntityRendererDispatcher]
           [net.minecraft.client.renderer IRenderTypeBuffer]
           [com.mojang.blaze3d.matrix MatrixStack]))

(defn create-tile-renderer [render-fn]
  (proxy [TileEntityRenderer] [TileEntityRendererDispatcher/INSTANCE]
    (render [tile-entity partial-ticks matrix-stack buffer combined-light]
      (render-fn tile-entity partial-ticks matrix-stack buffer combined-light))))

(defn register-renderer [registry block-id renderer]
  (swap! (:renderers registry) assoc block-id renderer))