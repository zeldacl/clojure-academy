(ns cn.li.mcmod.client.opengl
  (:import (com.mojang.blaze3d.platform GlStateManager GlStateManager$Color)
           (org.lwjgl.opengl GL11)))

(defn bind-texture [texture]
  (GlStateManager/_bindTexture texture))

(defn glColor [color]
  (GlStateManager/_color4f
    (:r color)
    (:g color)
    (:b color)
    (:a color)))

(defn glTexCoord2f [u v]
  ;(GlStateManager/texCoord2f u v)
  (GL11/glTexCoord2f u v))

(defn glVertex2f [x y]
  (GL11/glVertex2f x y))

(defn glColor4d [r g b a]
  (GL11/glColor4d r g b a))

(defmacro with-gl-quads [& body]
  `(do
     (GL11/glBegin GL11/GL_QUADS)
     ~@body
     (GL11/glEnd)))
