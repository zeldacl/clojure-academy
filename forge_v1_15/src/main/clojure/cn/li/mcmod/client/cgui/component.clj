(ns cn.li.mcmod.client.cgui.component
  (:require [cn.li.mcmod.client.resources :refer [load-texture]]
            [cn.li.mcmod.client.colors :refer [mono-blend]]
            [cn.li.mcmod.client.opengl :as gl]
            [cn.li.mcmod.client.hud-util :as hud-util]
            [cn.li.mcmod.client.cgui.core :refer [defcomponent Render]])
  (:import (net.minecraft.client Minecraft)
           (com.mojang.blaze3d.platform GlStateManager)
    ;(cn.li.mcmod.client.cgui.core Render)
           ))


;(defmacro defcomponent [name])

;(defcomponent text-box {
;                        :content ""
;                        :font ""
;                        :option ""
;                        :heightAlign ""
;                        :localized false
;                        :allowEdit false
;                        :emit true
;                        :doesEcho false
;                        :echo-char "*"
;                        :z-level 0
;                        :x-Offset 0
;                        :y-Offset 0
;                        :caret-pos 0
;                        :display-offset 0
;                        })

;(GlStateManager/bindTexture)
;(.render Minecraft/getInstance)

;(Minecraft.getMinecraft () .renderEngine.bindTexture (src)  )

;(defn blend_quad []
;  ())

(defcomponent ComponentBlendQuad [margin])

(def blend-quad-texture (load-texture "guis/blend_quad"))
(def line-texture (load-texture "guis/line"))
(def color (mono-blend (float 0.0), (float 0.5)))

(extend-protocol Render
  ComponentBlendQuad
  (render [this]
    (let [x 0 y 0 w 1 h 1
          margin (:margin this)
          widget (:widget this)
          quad (fn [col, row, x0, y0, x1, y1]
                 (let [u (/ col 3.0)
                       v (/ row 3.0)
                       step (/ 1.0 3.0)]
                   (doseq [[u v x y] [[u v x0 y0]
                                      [u (+ v step) x0 y1]
                                      [(+ u step) (+ v step) x1 y1]
                                      [(+ u step) v x1 y0]]]
                     (gl/glTexCoord2f u v)
                     (gl/glVertex2f x y))))]
      (gl/bind-texture blend-quad-texture)
      (gl/glColor color)

      (gl/with-gl-quads
        (let [xs [(- x margin) x (+ x w) (+ x w margin)]
              ys [(- y margin) y (+ y h) (+ y h margin)]]
          (doseq [[i j] (for [x (range 3)
                              y (range 3)] [x y])]
            (quad i j
              (nth xs i) (nth ys j)
              (nth xs (+ i 1) (nth ys (+ j 1)))))))

      (gl/glColor4d 1 1 1 1)

      (gl/bind-texture line-texture)
      (let [mrg 3.2
            x (- mrg)
            width (+ w (* mrg 2))]
        (hud-util/rect x -8.6 width 12)
        (hud-util/rect x (- h 2) width 8))
      )))
;(defcomponent blend-quad
;  :reder (fn [frame]
;           (let [quad (fn []
;                        )]
;             (gl/bind-texture blend-quad-texture)
;             (gl/glColor color))))