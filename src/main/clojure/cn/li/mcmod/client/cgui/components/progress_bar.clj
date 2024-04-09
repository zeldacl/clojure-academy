;(ns cn.lambdalib2.cgui.component.ProgressBar
;  (:require [cn.lambdalib2.cgui.Widget :as Widget]
;            [cn.lambdalib2.cgui.event.FrameEvent :as FrameEvent]
;            [cn.lambdalib2.util.Colors :as Colors]
;            [cn.lambdalib2.util.GameTimer :as GameTimer]
;            [cn.lambdalib2.util.HudUtils :as HudUtils]
;            [cn.lambdalib2.util.MathUtils :as MathUtils]
;            [org.lwjgl.opengl.GL11 :as GL11]
;            [org.lwjgl.util.Color :as Color]))
;
;(def Direction {:RIGHT 0 :LEFT 1 :UP 2 :DOWN 3})
;
;(defn- progress-bar []
;  (let [illustrating (atom false)
;        texture (atom nil)
;        dir (atom Direction/RIGHT)
;        progress (atom 0.0)
;        color (atom (Colors/white))]
;    (fn [this]
;      (Widget/listen this FrameEvent
;        (fn [wi e]
;          (when @illustrating
;            (reset! progress (* 0.5 (+ 1 (Math/sin (GameTimer/getAbsTime))))))
;
;          (let [disp (MathUtils/clampd 0 1 @progress)
;                [width height] (-> wi .transform (juxt :width :height))
;                [x y u v w h tw th] (case @dir
;                                      Direction/RIGHT [(list 0 0 0 0 disp width height disp 1)]
;                                      Direction/LEFT [(list (- width (* width disp)) 0 (- 1 disp) 0 (* width disp) height disp 1)]
;                                      Direction/UP [(list 0 (* height (- 1 disp)) 0 (- 1 disp) width (* height disp) 1 disp)]
;                                      Direction/DOWN [(list 0 0 0 0 width (* height disp) 1 disp)]
;                                      (throw (RuntimeException. "niconiconi, WTF??"))))
;          (if (and @texture (not= (.getPath @texture) "<null>"))
;            (HudUtils/loadTexture @texture)
;            (GL11/glDisable GL11/GL_TEXTURE_2D))
;
;          (Colors/bindToGL @color)
;          (apply HudUtils/rawRect x y u v w h tw th)
;          (GL11/glEnable GL11/GL_TEXTURE_2D)))))))
;atic ProgressBar get(Widget w) {
;                                return w.getComponent(ProgressBar.class);
;                                }
;
;}
