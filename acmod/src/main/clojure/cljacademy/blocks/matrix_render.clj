(ns cljacademy.blocks.matrix-render
  (:require [cljacademy.api.render :as render])
  (:import [net.minecraft.util ResourceLocation]))

(def ^:private matrix-texture
  (ResourceLocation. "cljacademy" "textures/blocks/wireless_matrix.png"))

(defprotocol IMatrixRender
  (render-base [this])
  (render-core [this])
  (render-plates [this])
  (render-shield [this time]))

(defrecord MatrixRenderState [animations]
  (update-animation! [this time]
    (swap! animations update :time + time)
    (when (> (:time @animations) 360.0)
      (swap! animations assoc :time 0.0))))

(defrecord MatrixRenderer [tile model state]
  IMatrixRender
  (render-base [_]
    (render/with-push-matrix
      (render/bind-texture matrix-texture)
      (render/render-model-part model "Main")))

  (render-core [_]
    (when (pos? (.getEnergy tile))
      (render/with-push-matrix
        (render/bind-texture matrix-texture)
        (render/render-model-part model "Core"))))

  (render-plates [_]
    (let [plate-count (.getPlateCount tile)]
      (render/with-push-matrix
        (render/bind-texture matrix-texture)
        (doseq [i (range plate-count)]
          (render/with-push-matrix
            (render/translate 0 (* i 0.25) 0)
            (render/render-model-part model (str "Plate" i)))))))

  (render-shield [_ time]
    (when (and (= (.getPlateCount tile) 3)
               (pos? (.getEnergy tile)))
      (let [shield-rotation (* time 2.0)
            shield-height (* 0.1 (Math/sin (* time Math/PI)))]
        (render/with-push-matrix
          (render/bind-texture matrix-texture)
          (render/translate 0 shield-height 0)
          (render/rotate shield-rotation [0 1 0])
          (render/render-model-part model "Shield"))))))

(defn create-renderer [tile]
  (let [model (render/load-model "cljacademy:models/block/wireless_matrix")
        state (->MatrixRenderState (atom {:time 0.0}))]
    (->MatrixRenderer tile model state)))