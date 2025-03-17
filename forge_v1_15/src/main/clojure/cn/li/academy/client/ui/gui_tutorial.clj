;(ns cn.academy.client.gui.GuiTutorial
;  (:import [cn.academy AcademyCraft Resources]
;           [cn.academy.client.render.util ACRenderingHelper]
;           [cn.academy.tutorial ACTutorial TutorialRegistry ViewGroup]
;           [cn.academy.tutorial.client ACMarkdownRenderer]
;           [cn.lambdalib2.cgui CGuiScreen Widget WidgetContainer]
;           [cn.lambdalib2.cgui.component Transform HeightAlign]
;           [cn.lambdalib2.cgui.event FrameEvent GuiEvent LeftClickEvent]
;           [cn.lambdalib2.cgui.loader CGUIDocument]
;           [cn.lambdalib2.registry StateEventCallback]
;           [cn.lambdalib2.util Colors HudUtils MathUtils]
;           [cn.lambdalib2.render.font IFont]
;           [cn.lambdalib2.util.markdown GLMarkdownRenderer MarkdownParser]
;           [com.google.common.base Preconditions]
;           [net.minecraft.client Minecraft]
;           [net.minecraft.entity.player EntityPlayer]
;           [net.minecraft.util ResourceLocation]
;           [org.apache.commons.lang3.tuple Pair]
;           [org.lwjgl.opengl GL11 GLU]
;           [org.lwjgl.util Color])
;  (:require [clojure.string :as str]))
;
;(def font (atom nil))
;(def fontBold (atom nil))
;(def fontItalic (atom nil))
;(def loaded (atom nil))
;
;(defn __init [ev]
;  (Resources/preloadMipmapTexture "guis/tutorial/logo0")
;  (Resources/preloadMipmapTexture "guis/tutorial/logo1")
;  (Resources/preloadMipmapTexture "guis/tutorial/logo2")
;  (Resources/preloadMipmapTexture "guis/tutorial/logo3")
;  (reset! loaded (CGUIDocument/read (ResourceLocation. "academy:guis/tutorial.xml")))
;  (reset! font (Resources/font))
;  (reset! fontBold (Resources/fontBold))
;  (reset! fontItalic (Resources/fontItalic)))
;
;(defn render-info [tut]
;  (let [cached (atom {})]
;    (if-not (@cached tut)
;      (let [raw (.getContent tut)
;            i1 (.indexOf raw "![title]")
;            i2 (.indexOf raw "![brief]")
;            i3 (.indexOf raw "![content]")]
;        (if (and (< i1 i2) (< i2 i3) (not= -1 i1))
;          (let [title (str/trim (subs raw (+ i1 8) i2))
;                brief (str/trim (subs raw (+ i2 8) i3))
;                content (str/trim (subs raw (+ i3 10)))]
;            (reset! cached (assoc @cached tut {:title title
;                                               :raw-brief brief
;                                               :raw-content content
;                                               :brief nil
;                                               :content nil})))
;          (throw (RuntimeException. (str "Malformed tutorial " (.id tut))))))
;      (get @cached tut))))
;
;(defn trim-head [str]
;  (let [idx (atom 0)]
;    (while (and (< @idx (count str))
;             (or (= (get str @idx) \space)
;               (= (get str @idx) \newline)
;               (= (get str @idx) \return)))
;      (swap! idx inc))
;    (subs str @idx)))
;
;(defn draw-screen [this mx my w]
;  (let [cached-width (atom -1)]
;    (when (not= @cached-width width)
;      (-> this .frame .transform (assoc :scale (/ width 480.0)) (assoc :dirty true))
;      (reset! cached-width width))
;    (CGuiScreen/drawScreen this mx my w)))
;
;(defn init-ui [this]
;  (let [frame (.getWidget @loaded "frame")
;        left-part (.getWidget frame "leftPart")
;        list-area (.getWidget left-part "list")
;        right-part (.getWidget frame "rightPart")
;        show-window (.getWidget right-part "showWindow")
;        right-window (.getWidget right-part "rightWindow")
;        center-part (.getWidget right-part "centerPart")
;        logo0 (.getWidget right-part "logo0")
;        logo1 (.getWidget right-part "logo1")
;        logo2 (.getWidget right-part "logo2")
;        logo3 (.getWidget right-part "logo3")
;        show-area (.getWidget show-window "area")
;        tag-area (.getWidget show-window "tag_area")]
;    (-> right-window .transform (assoc :doesDraw false))
;    (-> center-part .transform (assoc :doesDraw false))
;    (-> show-window .transform (assoc :doesDraw false))
;    (-> logo0 .transform (assoc :doesDraw false))
;    (-> logo1 .transform (assoc :doesDraw false))
;    (-> logo2 .transform (assoc :doesDraw false))
;    (-> logo3 .transform (assoc :doesDraw false))
;    (-> center-part .getWidget "text" (.listen FrameEvent (fn [w e]
;                                                            (when-let [current-tut this/currentTut]
;                                                              (let [renderer (render-info (.tut current-tut))
;                                                                    delta (* 10 (- 1 (-> center-part .getWidget "scroll_2" ACRenderingHelper/getProgress)))
;                                                                    ht (max 0 (- (.getMaxHeight renderer) (.transform w :height) 10))]
;                                                                (glPushMatrix)
;                                                                (glTranslated 0 0 10)
;                                                                (glColorMask false false false false)
;                                                                (glDepthMask true)
;                                                                (HudUtils/colorRect 0 0 (.transform w :width) (.transform w :height))
;                                                                (glColorMask true true true true)
;                                                                (glTranslated 3 (- 3 delta) 0)
;                                                                (glDepthFunc GL_EQUAL)
;                                                                (.render (renderer/getContent))
;                                                                (glDepthFunc GL_LEQUAL)
;                                                                (glPopMatrix))))))
;    (-> right-window .getWidget "text" (.listen FrameEvent (fn [w e]
;                                                             (when-let [current-tut this/currentTut]
;                                                               (let [info (render-info (.tut current-tut))]
;                                                                 (doto font
;                                                                   (.draw (.title info) 3 3 (FontOption. 10)))
;                                                                 (glPushMatrix)
;                                                                 (glTranslated 3 15 0)
;                                                                 (.render (info/getBrief))
;                                                                 (glPopMatrix)))))))
;  (defn GuiTutorial []
;    (let [player (.player Minecraft/getMinecraft)
;          p (TutorialRegistry/groupByLearned player)
;          learned (first p)
;          unlearned (second p)
;          first-open (let [tag-name "AC_Tutorial_Open"]
;                       (when-not (.getBoolean (.getEntityData player) tag-name)
;                         (.setBoolean (.getEntityData player) tag-name true))
;                       (not (.getBoolean (.getEntityData player) tag-name)))]
;      (CGuiScreen.)
;      (init-ui this)
;      this))
;
;  (defn update-view [this]
;    (let [view (currentView this)]
;      (when view
;        (let [scale (* 366.0 (/ width (.scale show-area)))
;              aspect (/ (.displayWidth Minecraft/getMinecraft) (.displayHeight Minecraft/getMinecraft))]
;          (glMatrixMode GL_PROJECTION)
;          (glPushMatrix)
;          (glLoadIdentity)
;          (glTranslated (- 1.0 (/ (+ (.scale show-area) (.x show-area)) width))
;            (- 1.0 (/ (+ (.scale show-area) (.y show-area)) height))
;            0)
;          (glScaled scale (* -1 scale aspect) -0.5)
;          (GLU/gluPerspective 50 1 1.0 100)
;          (glMatrixMode GL_MODELVIEW)
;          (glPushMatrix)
;          (glLoadIdentity)
;          (glTranslated 0 0 -4)
;          (glTranslated 0.55 0.55 0.5)
;          (glScaled 0.75 -0.75 0.75)
;          (glRotated -20 1 0 0.1)
;          (.post view (ViewRenderEvent.))
;          (glPopMatrix)
;          (glMatrixMode GL_PROJECTION)
;          (glPopMatrix)
;          (glMatrixMode GL_MODELVIEW)
;          (glEnable GL_DEPTH_TEST)
;          (glEnable GL_ALPHA_TEST)
;          (glCullFace GL_BACK)))))
;
;  (defn current-preview [this]
;    (when-let [current-tut this/currentTut]
;      (let [view-group (.getGroup (.tut current-tut))
;            sub-views (->> view-group
;                        (.getViews)
;                        (map #(when (= (.parent %) view-group) %))
;                        (remove nil?))]
;        (when (seq sub-views)
;          (let [view-index (or (.viewIndex this) 0)]
;            {:sub-views sub-views
;             :view-index view-index})))))
;
;  (defn current-view [this]
;    (when-let [preview-info (current-preview this)]
;      (let [sub-views (:sub-views preview-info)
;            view-index (:view-index preview-info)]
;        (when (seq sub-views)
;          (let [view (nth sub-views view-index)]
;            (when view
;              (let [view-type (.getType view)]
;                (case view-type
;                  :model
;                  (let [model (.getModel view)]
;                    (when model
;                      (let [model-type (.getType model)]
;                        (case model-type
;                          :obj
;                          (let [obj (.getObj model)]
;                            (when obj
;                              (let [model-info (-> obj
;                                                 (.getModelInfo)
;                                                 (.getRenderInfo))]
;                                (when model-info
;                                  {:type :model
;                                   :model-info model-info}))))
;                          :mc
;                          (let [mc (.getMC model)]
;                            (when mc
;                              {:type :model
;                               :mc mc}))))
;                      :image
;                      (let [image (.getImage model)]
;                        (when image
;                          {:type :image
;                           :image image})))))))))))
;
;    (defn update-view [this]
;      (when-let [view (current-view this)]
;        (let [show-area (.getWidget this "showWindow.area")]
;          (when show-area
;            (let [width (.width show-area)
;                  height (.height show-area)
;                  mc (Minecraft/getMinecraft)
;                  aspect (/ (.displayWidth mc) (.displayHeight mc))
;                  scale (/ 366.0 width (.scale frame))
;                  ln 500
;                  ln2 300
;                  cl 50
;                  ht 5.0]
;              (glMatrixMode GL_PROJECTION)
;              (glPushMatrix)
;              (glLoadIdentity)
;              (glTranslated (- 1.0 (/ (+ (.scale show-area) (.x show-area)) width))
;                (- 1.0 (/ (+ (.scale show-area) (.y show-area)) height))
;                0)
;              (glScaled scale (* -1 scale aspect) -0.5)
;              (GLU/gluPerspective 50 1 1.0 100)
;              (glMatrixMode GL_MODELVIEW)
;              (glPushMatrix)
;              (glLoadIdentity)
;              (glTranslated 0 0 -4)
;              (glTranslated 0.55 0.55 0.5)
;              (glScaled 0.75 -0.75 0.75)
;              (glRotated -20 1 0 0.1)
;              (.post view (ViewRenderEvent.))
;              (glPopMatrix)
;              (glMatrixMode GL_PROJECTION)
;              (glPopMatrix)
;              (glMatrixMode GL_MODELVIEW)
;              (glEnable GL_DEPTH_TEST)
;              (glEnable GL_ALPHA_TEST)
;              (glCullFace GL_BACK)))))))
;
;  (defn init-ui [this]
;    (let [loaded (.getWidget this "frame")
;          left-part (.getWidget loaded "leftPart")
;          list-area (.getWidget left-part "list")
;          right-part (.getWidget loaded "rightPart")
;          show-window (.getWidget right-part "showWindow")
;          right-window (.getWidget right-part "rightWindow")
;          center-part (.getWidget right-part "centerPart")
;          logo0 (.getWidget right-part "logo0")
;          logo1 (.getWidget right-part "logo1")
;          logo2 (.getWidget right-part "logo2")
;          logo3 (.getWidget right-part "logo3")
;          show-area (.getWidget show-window "area")
;          tag-area (.getWidget show-window "tag_area")]
;      (when show-area
;        (.transform (.transform show-area) (doto (Transform.) (.doesDraw false))))
;      (when right-window
;        (.transform (.transform right-window) (doto (Transform.) (.doesDraw false))))
;      (when center-part
;        (.transform (.transform center-part) (doto (Transform.) (.doesDraw false))))
;      (when center-part
;        (.getWidget center-part "text")
;        (.listen center-part FrameEvent
;          (fn [w e]
;            (when-let [current-tut this/currentTut
;                       renderer (-> (render-info (.tut current-tut))
;                                  (.getContent))]
;              (glPushMatrix)
;              (glTranslated 0 0 10)
;              (glColorMask false false false false)
;              (glDepthMask true)
;              (HudUtils/colorRect 0 0 (.width (.transform w)) (.height (.transform w)))
;              (glColorMask true true true true)
;              (let [ht (Math/max 0 (- (.getMaxHeight renderer) (.height (.transform w)) 10))
;                    delta (* (.getProgress (DragBar/get (.getWidget center-part "scroll_2"))) ht)]
;                (glTranslated 3 (- 3 delta) 0)
;                (glDepthFunc GL_EQUAL)
;                (.render renderer)
;                (glDepthFunc GL_LEQUAL)
;                (glPopMatrix)))))
;        (when right-window
;          (.getWidget right-window "text")
;          (.listen right-window FrameEvent
;            (fn [w e]
;              (when-let [current-tut this/currentTut
;                         info (render-info (.tut current-tut))]
;                (let [title (.title info)
;                      brief (.getBrief info)]
;                  (.draw font title 3 3 fo_descTitle)
;                  (glPushMatrix)
;                  (glTranslated 3 15 0)
;                  (.render brief)
;                  (glPopMatrix))))))
;        (when show-area
;          (.listen show-area FrameEvent
;            (fn [w e]
;              (let [view (current-view this)]
;                (when view
;                  (let [view-group (.getGroup (.tut (.currentTut this)))
;                        sub-views (->> view-group
;                                    (.getViews)
;                                    (map #(when (= (.parent %) view-group) %))
;                                    (remove nil?))]
;                    (when (seq sub-views)
;                      (let [view-index (or (.viewIndex this) 0)]
;                        (let [view (nth sub-views view-index)]
;                          (when view
;                            (let [view-type (.getType view)]
;                              (case view-type
;                                :model
;                                (let [model (.getModel view)]
;                                  (when model
;                                    (let [model-type (.getType model)]
;                                      (case model-type
;                                        :obj
;                                        (let [obj (.getObj model)]
;                                          (when obj
;                                            (let [model-info (-> obj
;                                                               (.getModelInfo)
;                                                               (.getRenderInfo))]
;                                              (when model-info
;                                                {:type :model
;                                                 :model-info model-info}))))
;                                        :mc
;                                        (let [mc (.getMC model)]
;                                          (when mc
;                                            {:type :model
;                                             :mc mc}))))
;                                    :image
;                                    (let [image (.getImage model)]
;                                      (when image
;                                        {:type :image
;                                         :image image})))))))))))))))
;          (when show-window
;            (.getWidget show-window "btn_left")
;            (.listen (.getWidget show-window "btn_left") LeftClickEvent
;              (fn [w e]
;                (when-let [info (current-preview this)]
;                  (let [view-index (dec (:view-index info))
;                        sub-views (:sub-views info)]
;                    (when (< view-index 0)
;                      (assoc info :view-index (dec (count sub-views))))
;                    (update-view this)))))
;            (.getWidget show-window "btn_right")
;            (.listen (.getWidget show-window "btn_right") LeftClickEvent
;              (fn [w e]
;                (when-let [info (current-preview this)]
;                  (let [view-index (inc (:view-index info))
;                        sub-views (:sub-views info)]
;                    (assoc info :view-index (mod view-index (count sub-views))))
;                  (update-view this)))))
;          (let [option (FontOption. 10)]
;            (.listen tag-area FrameEvent
;              (fn [w evt]
;                (let [hovering (.getHoveringWidget gui)]
;                  (when hovering
;                    (let [comp (.getComponent hovering ViewGroupButton)]
;                      (when comp
;                        (.draw font (.getDisplayText (.group comp)) 0 -8 option))))))))
;          (rebuild-list this)
;          (let [ln 500 ln2 300 cl 50 ht 5]
;            (if-not first-open
;              (.listen logo1 FrameEvent
;                (fn [w e]
;                  (glPushMatrix)
;                  (.glTranslated logo1.transform.width / 2 logo1.transform.height / 2 + 15 0)
;                  (lineglow this (- ln2 ln) ln ht)
;                  (lineglow this (- ln) (- ln2) ht)
;                  (glPopMatrix)))
;              (do
;                (set! (.doesDraw list-area.transform) false)
;                (let [b1 0.3 b2 0.2
;                      start-time (GameTimer/getAbsTime)]
;                  (.listen logo2 FrameEvent
;                    (fn [__ e]
;                      (blend this logo2 0.65 0.3)
;                      (blend this logo0 1.75 0.3)
;                      (blend this left-part 1.75 0.3)
;                      (blend this logo1 1.3 0.3)
;                      (blend this logo3 0.1 0.3)
;                      (blendy this logo3 0.7 0.4 63 -36)
;                      (let [dt (- (GameTimer/getAbsTime) start-time 0.4)]
;                        (when (> dt 0)
;                          (let [len (MathUtils/lerp 0 ln (/ dt b1))]
;                            (when (> len cl)
;                              (lineglow this cl len ht)
;                              (lineglow this (- len) (- cl) ht))))
;                        (when (> dt b1)
;                          (let [ldt (min (- dt b1) b2)
;                                len ln
;                                len2 (MathUtils/lerp (- ln (* 2 cl)) ln2 (/ ldt b2))]
;                            (lineglow this (- ln len2) len ht)
;                            (lineglow this (- len) (- ln len2) ht)))
;                        (set! (.doesDraw list-area.transform) (> dt 2.0))))))))
;            (.addWidget gui "frame" frame))
;          GroupButton extends Component {
;
;                                         public final ViewGroup group;
;
;                                         public ViewGroupButton(ViewGroup _group) {
;                                                                                   super("VGB");
;                                                                                   group = _group;
;                                                                                   }
;
;                                         }
;
;          private void debug(Object msg) {
;                                          AcademyCraft.log.info("[Tut] " + msg);
;                                          }
;
;          public class ViewRenderEvent implements GuiEvent {
;                                                            }
;}
