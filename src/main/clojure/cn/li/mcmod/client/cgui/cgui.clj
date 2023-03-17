(ns cn.li.mcmod.client.cgui.cgui
  (:import (com.mojang.blaze3d.platform GlStateManager GlStateManager$SourceFactor GlStateManager$DestFactor)))



(defrecord Aaa [width height])

(defn make-widget-container []
  {:widgets    {}
   :widgetList []})

(defn create-cgui []
  {
   :width 0
   :height 0
   :mouseX 0
   :mouseY 0
   :focus nil
:eventBus nil
   })

(defn draw
  ([gui] (draw gui -1 -1))
  ([gui mouse-x mouse-y]
   ;frameUpdate();
   ;        updateMouse(mx, my);
   (GlStateManager/_disableAlphaTest)
   (GlStateManager/_blendFunc GlStateManager$SourceFactor/SRC_ALPHA GlStateManager$DestFactor/ONE_MINUS_SRC_ALPHA)
   ;drawTraverse(mx, my, null, this, getTopWidget(mx, my));
   ;if (debug) {
   ;            Widget hovering = getHoveringWidget();
   ;            if (hovering != null) {
   ;                GL11.glColor4f(1, .5f, .5f, .8f);
   ;                HudUtils.drawRectOutline(hovering.x, hovering.y,
   ;                        hovering.transform.width * hovering.scale,
   ;                        hovering.transform.height * hovering.scale, 3);
   ;                IFont font = TrueTypeFont.defaultFont;
   ;                font.draw(hovering.getFullName(), hovering.x, hovering.y - 10, new FontOption(10));
   ;            }
   ;
   ;        }
   (GlStateManager/_enableAlphaTest)
   ))
