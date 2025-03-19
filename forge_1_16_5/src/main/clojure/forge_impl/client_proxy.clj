(ns forge-impl.client-proxy
  (:require [forge-impl.gui-registry :as gui]
            [forge-impl.renderer :as renderer])
  (:import [net.minecraftforge.fml.client.registry ClientRegistry]
           [net.minecraftforge.api.distmarker Dist OnlyIn]))

@OnlyIn(Dist/CLIENT)
(defn init []
  ;; Register container screens
  (gui/register-client-gui "wireless_matrix")
  
  ;; Register special renderers
  (ClientRegistry/bindTileEntityRenderer
    cn.academy.tile_entities.wireless_matrix_te.WirelessMatrixTE
    (fn [dispatcher]
      (renderer/create-tile-renderer
        cn.academy.renderers.wireless-matrix-renderer/render-energy-field))))