;(ns cn.li.mcmod.client.cgui.cgui
;  (:import (com.mojang.blaze3d.platform GlStateManager GlStateManager$SourceFactor GlStateManager$DestFactor)))
;
;
;
;(defrecord Aaa [width height])
;
;(defn make-widget-container []
;  {:widgets    {}
;   :widgetList []})
;
;(defn create-cgui []
;  {
;   :width 0
;   :height 0
;   :mouseX 0
;   :mouseY 0
;   :focus nil
;:eventBus nil
;   })
;
;(defn draw
;  ([gui] (draw gui -1 -1))
;  ([gui mouse-x mouse-y]
;   ;frameUpdate();
;   ;        updateMouse(mx, my);
;   (GlStateManager/_disableAlphaTest)
;   (GlStateManager/_blendFunc GlStateManager$SourceFactor/SRC_ALPHA GlStateManager$DestFactor/ONE_MINUS_SRC_ALPHA)
;   ;drawTraverse(mx, my, null, this, getTopWidget(mx, my));
;   ;if (debug) {
;   ;            Widget hovering = getHoveringWidget();
;   ;            if (hovering != null) {
;   ;                GL11.glColor4f(1, .5f, .5f, .8f);
;   ;                HudUtils.drawRectOutline(hovering.x, hovering.y,
;   ;                        hovering.transform.width * hovering.scale,
;   ;                        hovering.transform.height * hovering.scale, 3);
;   ;                IFont font = TrueTypeFont.defaultFont;
;   ;                font.draw(hovering.getFullName(), hovering.x, hovering.y - 10, new FontOption(10));
;   ;            }
;   ;
;   ;        }
;   (GlStateManager/_enableAlphaTest)
;   ))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;ai
;(ns CGui
;  (:import [java.util Iterator])
;  (:import [cn.lambdalib2.cgui.event AddWidgetEvent DragEvent DragStopEvent GainFocusEvent KeyEvent LeftClickEvent LostFocusEvent MouseClickEvent RefreshEvent RightClickEvent])
;  (:import [cn.lambdalib2.cgui.component Transform])
;  (:import [cn.lambdalib2.render.font IFont FontOption TrueTypeFont])
;  (:import [cn.lambdalib2.util Debug GameTimer HudUtils MathUtils])
;  (:import [org.lwjgl.opengl GL11])
;  (:import [org.lwjgl.util.glu GLU]))
;
;(def DRAG_TIME_TOLE 0.1)
;
;(defn- gtnTraverse [x y cur set]
;  (if (and cur (.isVisible cur))
;    (let [top (.getTopWidget cur x y)]
;      (if (and top (not= top cur))
;        (recur top top)
;        cur))
;    (let [iter (.iterator set)]
;      (while (.hasNext iter)
;        (let [widget (.next iter)]
;          (when (not= widget cur)
;            (let [res (gtnTraverse x y widget widget)]
;              (when res
;                (return res)))))))))
;
;(defn- drawTraverse [mx my cur set top]
;  (try
;    (when (and cur (.isVisible cur))
;      (GL11/glPushMatrix)
;      (GL11/glTranslated (.x cur) (.y cur) 0)
;      (let [scale (.scale cur)]
;        (when (not= scale 1)
;          (GL11/glScaled scale scale 1)))
;      (.draw cur mx my)
;      (let [iter (.iterator cur)]
;        (while (.hasNext iter)
;          (let [widget (.next iter)]
;            (drawTraverse mx my widget widget top))))
;      (GL11/glPopMatrix)))))
;
;(defn- updateTraverse [cur set]
;  (when cur
;    (when (.dirty cur)
;      (.post cur (RefreshEvent.))
;      (.updateWidget this cur)))
;  (let [iter (.iterator set)]
;    (while (.hasNext iter)
;      (let [widget (.next iter)]
;        (when (not= widget cur)
;          (when (not (.disposed widget))
;            (updateTraverse widget widget)
;            (.update widget)))))))
;
;(defn- updateMouse [mx my]
;  (reset! mouseX mx)
;  (reset! mouseY my))
;
;(defn- moveWidgetToAbsPos [widget x0 y0]
;  (let [transform (.transform widget)
;        tx (if (.isWidgetParent widget)
;             (.x (.getWidgetParent widget))
;             0)
;        ty (if (.isWidgetParent widget)
;             (.y (.getWidgetParent widget))
;             0)
;        tw (if (.isWidgetParent widget)
;             (* (.scale (.getWidgetParent widget)) (.transform.width (.getWidgetParent widget)))
;             width)
;        th (if (.isWidgetParent widget)
;             (* (.scale (.getWidgetParent widget)) (.transform.height (.getWidgetParent widget)))
;             height)
;        parentScale (if (.isWidgetParent widget)
;                      (.scale (.getWidgetParent widget))
;                      1)]
;    (reset! (.x widget) x0)
;    (reset! (.y widget) y0)
;    (reset! (.dirty widget) true)
;    (reset! (.x transform)
;      (/ (- x0 tx (* (.alignWidth transform) (- tw (* (.scale widget) (.transform.width transform))))) parentScale))
;    (reset! (.y transform)
;      (/ (- y0 ty (* (.alignHeight transform) (- th (* (.scale widget) (.transform.height transform))))) parentScale))))
;
;(defn- getDraggingWidget []
;  (let [time (GameTimer/getAbsTime)]
;    (if (or (not= draggingNode nil) (> (- time lastDragTime) DRAG_TIME_TOLE))
;      draggingNode
;      nil)))
;
;(defn- gainFocus [node]
;  (when (not= node focus)
;    (when (not= focus nil)
;      (removeFocus node))
;    (reset! focus node)
;    (.post node (GainFocusEvent.)))))
;
;(defn- removeFocus
;  ([]
;   (removeFocus nil))
;  ([newFocus]
;   (when (not= focus nil)
;     (.post focus (LostFocusEvent. newFocus))
;     (reset! focus nil)))))
;
;(defn- postMouseEv [target bus mx my bid local]
;  (let [x (if local (/ (- mx (.x target)) (.scale target)) mx)
;        y (if local (/ (- my (.y target)) (.scale target)) my)]
;    (.postEvent bus (MouseClickEvent. x y bid))
;    (when (= bid 0)
;      (.postEvent bus (LeftClickEvent. x y)))
;    (when (= bid 1)
;      (.postEvent bus (RightClickEvent. x y)))))
;
;(defn- mouseClicked [mx my bid]
;  (updateMouse mx my)
;  (postMouseEv nil eventBus mx my bid false)
;  (let [top (getTopWidget mx my)]
;    (when top
;      (when (= bid 0)
;        (gainFocus top))
;      (removeFocus)
;      (postMouseEv top (.eventBus top) mx my bid true)
;      true))))
;
;(defn- updateWidget [widget]
;  (when (not= (.dirty widget) false)
;    (when (.isWidgetParent widget)
;      (let [p (.getWidgetParent widget)]
;        (reset! (.scale widget) (* (.scale (.transform widget)) (.scale p)))))
;    (let [transform (.transform widget)
;          tx (if (.isWidgetParent widget)
;               (.x (.getWidgetParent widget))
;               0)
;          ty (if (.isWidgetParent widget)
;               (.y (.getWidgetParent widget))
;               0)
;          tw (if (.isWidgetParent widget)
;               (* (.scale (.getWidgetParent widget)) (.transform.width (.getWidgetParent widget)))
;               width)
;          th (if (.isWidgetParent widget)
;               (* (.scale (.getWidgetParent widget)) (.transform.height (.getWidgetParent widget)))
;               height)
;          parentScale (if (.isWidgetParent widget)
;                        (.scale (.getWidgetParent widget))
;                        1)]
;      (reset! (.x widget) (+ tx (* (.alignWidth transform) (- tw (* (.scale widget) (.transform.width transform)))) (* (.x transform) parentScale)))
;      (reset! (.y widget) (+ ty (* (.alignHeight transform) (- th (* (.scale widget) (.transform.height transform)))) (* (.y transform) parentScale)))
;      (reset! (.dirty widget) false)
;      (doseq [w (.iterator widget)]
;        (updateWidget w)))
;
;    (defn- draw []
;      (draw -1 -1))
;
;    (defn- draw [mx my]
;      (let [time (GameTimer/getAbsTime)]
;        (when (= lastFrameTime -1)
;          (reset! deltaTime 0)
;          (reset! lastFrameTime time))
;        (reset! deltaTime (MathUtils/clampd 0 0.1 (- time lastFrameTime)))
;        (reset! lastFrameTime time))
;      (updateMouse mx my)
;      (GL11/glDisable GL11/GL_ALPHA_TEST)
;      (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)
;      (drawTraverse mx my nil this (getTopWidget mx my))
;      (when debug
;        (let [hovering (getHoveringWidget)]
;          (when hovering
;            (GL11/glColor4f 1 .5 .5 .8)
;            (HudUtils/drawRectOutline (.x hovering) (.y hovering) (* (.transform.width hovering) (.scale hovering)) (* (.transform.height hovering) (.scale hovering)) 3)
;            (let [font TrueTypeFont/defaultFont]
;              (.draw font (.getFullName hovering) (.x hovering) (- (.y hovering) 10) (FontOption. 10))))))
;
;      (GL11/glEnable GL11/GL_ALPHA_TEST))
;
;    (defn- mouseClickMove [mx my btn dt]
;      (updateMouse mx my)
;      (when (= btn 0)
;        (let [time (GameTimer/getAbsTime)]
;          (when (= draggingNode nil)
;            (reset! lastStartTime time)
;            (reset! draggingNode (getTopWidget mx my))
;            (when (= draggingNode nil)
;              (return false))
;            (reset! xOffset (- mx (.x draggingNode)))
;            (reset! yOffset (- my (.y draggingNode))))
;          (reset! lastDragTime time)
;          (.post draggingNode (DragEvent. xOffset yOffset))
;          true)))
;
;    (defn- resize [w h]
;      (let [diff (or (not= width w) (not= height h))]
;        (reset! width w)
;        (reset! height h)
;        (when diff
;          (doseq [widget this]
;            (reset! (.dirty widget) true)))))
;
;    (defn- addWidget [name w]
;      (when (not (.hasWidget this name))
;        (super/addWidget name w)
;        (.postEvent eventBus (AddWidgetEvent. w))
;        true))
;
;    (defn- keyTyped [ch key]
;      (when focus
;        (.post focus (KeyEvent. ch key))))
;
;    (defn- getTopWidget [x y]
;      (gtnTraverse x y nil this))
;
;    (defn- getHoveringWidget []
;      (getTopWidget @mouseX @mouseY))
;
;    (defn- gtnTraverse [x y node set]
;      (let [res (if (and node (.isVisible node) (.doesListenKey (.transform node)) (.isPointWithin node x y))
;                  node
;                  nil)
;            next (reduce #(or %1 %2) nil (map #(gtnTraverse x y % %) set))]
;        (if next
;          next
;          res)))
;
;    (defn- postEvent [event]
;      (.postEvent eventBus nil event))
;
;    (defn- postEventHierarchically [event]
;      (postEvent event)
;      (doseq [w (getDrawList)]
;        (hierPostEvent w event)))
;
;    (defn- hierPostEvent [w event]
;      (.post w event)
;      (doseq [ww (.widgetList w)]
;        (hierPostEvent ww event)))
;
;    (defn- listen [clazz handler]
;      (.listen eventBus clazz handler 0))
;
;    (defn- unlisten [clazz handler]
;      (.unlisten eventBus clazz handler))
;
;    (defn- mouseClicked [mx my bid]
;      (updateMouse mx my)
;      (postMouseEv nil eventBus mx my bid false)
;      (let [top (getTopWidget mx my)]
;        (when top
;          (when (= bid 0)
;            (gainFocus top))
;          (removeFocus)
;          (postMouseEv top (.eventBus top) mx my bid true)
;          true))))
;
;  (defn- frameUpdate []
;    (let [time (GameTimer/getAbsTime)]
;      (when (= lastFrameTime -1)
;        (reset! deltaTime 0)
;        (reset! lastFrameTime time))
;      (reset! deltaTime (MathUtils/clampd 0 0.1 (- time lastFrameTime)))
;      (reset! lastFrameTime time))
;    (when (not= draggingNode nil)
;      (when (> (- time lastDragTime) DRAG_TIME_TOLE)
;        (.post draggingNode (DragStopEvent.))
;        (reset! draggingNode nil)))
;    (updateTraverse nil this)
;    (.update this)))
;
;(defn- keyPressed [key]
;  (when focus
;    (.post focus (KeyEvent. nil key))))
;
;(defn- onWidgetAdded [name w]
;  (reset! (.gui w) this)
;  (updateWidget w))
;, handler);
;}
;
;public void postEvent(GuiEvent event) {
;                                       eventBus.postEvent(null, event);
;                                       }
;/**
;* Event bus delegator, will post every widget inside this CGui. <br>
;* Note that this might impact peformance when used incorectlly.
;*/
;    public void postEventHierarchically(GuiEvent event) {
;                                                           eventBus.postEvent(null, event);
;                                                           for(Widget w : getDrawList()) {
;                                                                                          hierPostEvent(w, event);
;                                                                                          }
;                                                           }
;
;private void hierPostEvent(Widget w, GuiEvent event) {
;                                                      w.post(event);
;                                                      for(Widget ww : w.widgetList) {
;                                                                                     hierPostEvent(ww, event);
;                                                                                     }
;                                                      }
;}
;t(event);
;for(Widget ww : w.widgetList) {
;                               hierPostEvent(ww, event);
;                               }
;}
;}
;Event event) {
;              w.post(event);
;              for(Widget ww : w.widgetList) {
;                                             hierPostEvent(ww, event);
;                                             }
;              }
;}
;t(event);
;for(Widget ww : w.widgetList) {
;                               hierPostEvent(ww, event);
;                               }
;}
;}
;t(event);
;for(Widget ww : w.widgetList) {
;                               hierPostEvent(ww, event);
;                               }
;}
;}
