(ns cn.li.mcmod.client.hud-util
  (:import (net.minecraft.client.renderer Tessellator BufferBuilder)
           (org.lwjgl.opengl GL11)
           (net.minecraft.client.renderer.vertex DefaultVertexFormats)))

(def z-level 0)

(defn addVertexWithUV [^BufferBuilder bb x y z u v]
  (doto bb
    (.pos x y z)
    (.tex u v)
    (.endVertex)))

(defn raw-rect [x y u v width height texWidth texHeight]
  (let [t (Tessellator/getInstance)
        bb (.getBuffer t)]
    (.begin bb GL11/GL_QUADS DefaultVertexFormats/POSITION_TEX)
    (addVertexWithUV bb (+ x 0) (+ y height) z-level (+ u 0) (+ v texHeight))
    (addVertexWithUV bb (+ x width) (+ y height) z-level (+ u texWidth) (+ v texHeight))
    (addVertexWithUV bb (+ x width) (+ y 0) z-level (+ u texWidth) (+ v 0))
    (addVertexWithUV bb (+ x 0) (+ y 0) z-level (+ u 0) (+ v 0))
    (.draw t)))

(defn rect [x y width height]
  (raw-rect x y 0 0 width height 1 1))
