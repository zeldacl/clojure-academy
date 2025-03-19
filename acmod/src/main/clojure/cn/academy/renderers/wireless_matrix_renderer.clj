(ns cn.academy.renderers.wireless-matrix-renderer
  (:require [mcmod.capabilities :as cap])
  (:import [com.mojang.blaze3d.matrix MatrixStack]
           [net.minecraft.client.renderer IRenderTypeBuffer RenderType]
           [net.minecraft.util.math.vector Matrix4f Vector3f]
           [net.minecraft.client.renderer.texture OverlayTexture]))

(defn render-energy-field [tile-entity ^MatrixStack matrix partial-ticks ^IRenderTypeBuffer buffer light]
  (when-let [energy-storage (cap/get-capability tile-entity "forge:energy" nil)]
    (let [energy-percent (/ (float (cap/get-energy-stored energy-storage))
                           (float (cap/get-max-energy-stored energy-storage)))
          matrix4f (.last matrix)
          buffer (.getBuffer buffer (RenderType/getLightning))
          time (/ (System/currentTimeMillis) 1000.0)
          height (* 1.0 energy-percent)]
      
      ;; Push matrix state
      (.pushPose matrix)
      
      ;; Translate to center and apply animation
      (.translate matrix 0.5 0.5 0.5)
      (.mulPose matrix (Vector3f/YP. (* time 20)))
      
      ;; Draw energy field effect
      (doto buffer
        (.vertex matrix4f -0.5 0.0 -0.5)
        (.color 0.5 0.8 1.0 (* 0.3 energy-percent))
        (.endVertex)
        
        (.vertex matrix4f 0.5 height 0.5)
        (.color 0.5 0.8 1.0 (* 0.7 energy-percent))
        (.endVertex)
        
        (.vertex matrix4f 0.5 0.0 -0.5)
        (.color 0.5 0.8 1.0 (* 0.3 energy-percent))
        (.endVertex))
      
      ;; Pop matrix state
      (.popPose matrix))))