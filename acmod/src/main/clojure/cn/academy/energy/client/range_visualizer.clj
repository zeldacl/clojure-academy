(ns cn.academy.energy.client.range-visualizer
  (:require [cn.academy.energy.api.wireless :as wireless])
  (:import [net.minecraft.client.renderer BufferBuilder]
           [com.mojang.blaze3d.vertex IVertexBuilder]
           [net.minecraft.client.renderer.vertex DefaultVertexFormats]
           [net.minecraft.client.renderer.tileentity TileEntityRendererDispatcher]
           [net.minecraft.util.math BlockPos]
           [org.lwjgl.opengl GL11]))

(def ^:private SPHERE_PRECISION 32)
(def ^:private RANGE_COLOR [0.2 0.6 1.0 0.15]) ; Light blue, transparent

(defn- generate-sphere-vertices [radius precision]
  (for [phi (range 0 Math/PI (/ Math/PI precision))
        theta (range 0 (* 2 Math/PI) (/ (* 2 Math/PI) precision))]
    [(* radius (Math/sin phi) (Math/cos theta))
     (* radius (Math/cos phi))
     (* radius (Math/sin phi) (Math/sin theta))]))

(defn render-range-sphere [pos range active?]
  (GL11/glPushMatrix)
  (GL11/glTranslatef 
    (float (+ (.getX pos) 0.5))
    (float (+ (.getY pos) 0.5))
    (float (+ (.getZ pos) 0.5)))
  
  ; Enable transparency and disable depth writing
  (GL11/glEnable GL11/GL_BLEND)
  (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)
  (GL11/glDisable GL11/GL_DEPTH_TEST)
  
  (let [buffer (BufferBuilder. 256)
        vertices (generate-sphere-vertices range SPHERE_PRECISION)
        [r g b a] (if active? 
                    RANGE_COLOR
                    [1.0 0.2 0.2 0.15])] ; Red for inactive
    
    (.begin buffer 7 DefaultVertexFormats/POSITION_COLOR) ; GL_QUADS
    
    (doseq [[x y z] vertices]
      (.pos buffer x y z)
      (.color buffer r g b a)
      (.endVertex buffer))
    
    (.finishDrawing buffer))
  
  (GL11/glPopMatrix)
  (GL11/glEnable GL11/GL_DEPTH_TEST)
  (GL11/glDisable GL11/GL_BLEND))

(defn render-node-range [node]
  (when (and node (.isHoldingFreqTool *client-player*))
    (let [pos (BlockPos. (.getPos node))
          range (.getRange node)
          active? (wireless/is-node-linked node)]
      (render-range-sphere pos range active?))))

(defn render-matrix-range [matrix]
  (when (and matrix (.isHoldingFreqTool *client-player*))
    (let [pos (BlockPos. (.getPos matrix))
          range (.getRange matrix)
          active? (wireless/is-matrix-active matrix)]
      (render-range-sphere pos range active?))))