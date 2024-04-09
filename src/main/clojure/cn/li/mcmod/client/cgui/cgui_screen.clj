;(ns cn.lambdalib2.cgui.CGuiScreen
;  (:import [net.minecraft.client.gui GuiScreen]
;           [org.lwjgl.opengl GL11]
;           [java.io IOException])
;  (:require [cn.lambdalib2.cgui :as cgui]))
;
;(defn- draw-screen [mx my w]
;  (cgui/resize width height)
;  (if drawBack
;    (drawDefaultBackground))
;  (GL11/glPushMatrix)
;  (GL11/glEnable GL11/GL_BLEND)
;  (cgui/draw mx my)
;  (GL11/glDisable GL11/GL_BLEND)
;  (GL11/glPopMatrix))
;
;(defn- mouse-clicked [mx my btn]
;  (cgui/mouseClicked mx my btn))
;
;(defn- mouse-click-move [mx my btn time]
;  (cgui/mouseClickMove mx my btn time))
;
;(defn- on-gui-closed []
;  (cgui/dispose))
;
;(defn- key-typed [par1 par2]
;  (super/keyTyped par1 par2)
;  (cgui/keyTyped par1 par2))
;
;(defn- get-gui []
;  gui)
;
;(defn- set-draw-back [flag]
;  (set! drawBack flag)
;  this)
;
;(defn- c-gui-screen [gui]
;  (let [gui (or gui (cgui/c-gui))]
;    (reify GuiScreen
;      (drawScreen [mx my w] (draw-screen mx my w))
;      (mouseClicked [mx my btn] (mouse-clicked mx my btn))
;      (mouseClickMove [mx my btn time] (mouse-click-move mx my btn time))
;      (onGuiClosed [] (on-gui-closed))
;      (keyTyped [par1 par2] (key-typed par1 par2)))
;    (set-draw-back true))))
;
;(defn- c-gui-screen []
;  (c-gui-screen nil))
;gui.keyTyped(par1, par2);
;}
;
;public CGui getGui() {
;                      return gui;
;                      }
;
;}
