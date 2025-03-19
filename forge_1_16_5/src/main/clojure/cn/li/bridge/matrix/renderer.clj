(ns cn.li.bridge.matrix.renderer
  (:require [cn.li.bridge.matrix.api :as matrix]
            [cn.li.bridge.matrix.particles :as particles]
            [clojure.tools.logging :as log])
  (:import [com.mojang.blaze3d.matrix MatrixStack]
           [com.mojang.blaze3d.systems RenderSystem]
           [net.minecraft.client.renderer.texture TextureAtlasSprite]
           [net.minecraft.client.renderer BufferBuilder WorldRenderer]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.client Minecraft]))

(def ^:private TEXTURE_PATH (ResourceLocation. "li:textures/matrix/energy_field.png"))
(def ^:private ENERGY_COLOR [0.3 0.6 1.0 0.4]) ; Blue energy field
(def ^:private FORMED_COLOR [0.0 1.0 0.3 0.4]) ; Green when formed
(def ^:private ERROR_COLOR [1.0 0.2 0.2 0.4])  ; Red when invalid

(defn- setup-render-state! []
  (RenderSystem/enableBlend)
  (RenderSystem/defaultBlendFunc)
  (RenderSystem/disableTexture)
  (RenderSystem/depthMask false))

(defn- cleanup-render-state! []
  (RenderSystem/enableTexture)
  (RenderSystem/depthMask true)
  (RenderSystem/disableBlend))

(defn- draw-energy-field! [^MatrixStack matrix-stack ^BufferBuilder buffer pos scale [r g b a]]
  (let [x (- (.getX pos) (.getXOffset buffer))
        y (- (.getY pos) (.getYOffset buffer))
        z (- (.getZ pos) (.getZOffset buffer))]
    (.begin buffer 7)  ; GL_QUADS
    (doto buffer
      (.vertex matrix-stack x y z)
      (.color r g b a)
      (.endVertex)
      ; Add remaining vertices for cube faces
      )))

(defrecord MatrixRenderer [matrix]
  IMatrixRenderer
  (render [_ matrix-stack pos partial-ticks]
    (when (matrix/is-formed? matrix)
      (let [buffer (WorldRenderer/renderBuffer)
            energy-percent (/ (matrix/get-energy-stored matrix)
                            (matrix/get-energy-capacity matrix))
            color (cond
                   (not (matrix/validate-structure matrix)) ERROR_COLOR
                   (matrix/is-formed? matrix) FORMED_COLOR
                   :else ENERGY_COLOR)
            scale (+ 1.0 (* 0.2 energy-percent))]
        
        (setup-render-state!)
        (draw-energy-field! matrix-stack buffer pos scale color)
        (cleanup-render-state!)
        
        ; Spawn ambient particles based on state
        (when (zero? (mod (System/currentTimeMillis) 1000))
          (if (matrix/is-formed? matrix)
            (particles/spawn-formation-particles! (.level matrix) pos)
            (particles/spawn-error-particles! (.level matrix) pos)))))))

(defn create-renderer [matrix]
  (->MatrixRenderer matrix))