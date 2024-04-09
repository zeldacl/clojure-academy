;(ns cn.li.academy.client.auxgui.CPBar
;  (:require [cn.academy.ability.context :as ContextManager]
;            [cn.academy.datapart :as DataPart]
;            [cn.academy.event.ability :as PresetSwitchEvent]
;            [cn.academy.client.render.util :as ACRenderingHelper]
;            [cn.academy.Resources :as Resources]
;            [cn.lambdalib2.cgui :as cgui]
;            [cn.lambdalib2.cgui.component :as component]
;            [cn.lambdalib2.cgui.event :as event]
;            [cn.lambdalib2.registry :as registry]
;            [cn.lambdalib2.render.legacy :as legacy]
;            [cn.lambdalib2.util :as util]
;            [cn.lambdalib2.vis.curve :as curve]
;            [net.minecraftforge.fml.common.event :as FMLInitializationEvent]
;            [net.minecraftforge.fml.common.eventhandler :as SubscribeEvent]
;            [net.minecraftforge.fml.relauncher.Side :as Side]
;            [net.minecraft.client :as Minecraft]
;            [net.minecraft.util :as ResourceLocation]
;            [org.lwjgl.opengl :as GL11]
;            [javax.vecmath :as vecmath]
;            [java.util :as util]
;            [clojure.math.numeric-tower :as math]))
;
;(def instance (cgui/Widget.))
;
;(def WIDTH 964)
;(def HEIGHT 147)
;(def SCALE 0.2)
;
;(def CP_BALANCE_SPEED 2.0)
;(def O_BALANCE_SPEED 2.0)
;
;(def sin41 (math/sin (math/to-radians 44.0)))
;
;(def chProvider (atom nil))
;
;(def TEX_BACK_NORMAL (tex "back_normal"))
;(def TEX_BACK_OVERLOAD (tex "back_overload"))
;(def TEX_CP (tex "cp"))
;(def TEX_FRONT_OVERLOAD (tex "front_overload"))
;(def TEX_OVERLOADED (tex "overloaded"))
;(def TEX_OVERLOAD_HIGHLIGHT (tex "highlight_overload"))
;(def TEX_MASK (tex "mask"))
;
;(def cpColors (util/ArrayList.))
;(def overrideColors (util/ArrayList.))
;
;(def presetChangeTime (atom 0))
;(def lastPresetTime (atom 0))
;
;(def lastFrameActive (atom false))
;(def lastDrawTime (atom 0))
;(def showTime (atom 0))
;
;(def showingNumbers (atom false))
;(def lastShowValueChange (atom 0))
;
;(def bufferedCP (atom 0.0))
;(def bufferedOverload (atom 0.0))
;
;(def shaderLoaded (atom false))
;
;(def overlayTexture (atom nil))
;
;;; Inteference display
;
;(def maxtime (atom 0))
;(def frames (util/ArrayList.))
;(def alphaCurve (curve/CubicCurve.))
;
;;; 初始化干扰显示
;(let [aspect (/ WIDTH HEIGHT)
;      offsetMax 9
;      iteration 60]
;  (.addPoint alphaCurve 0 (util/ranged 0.2 0.8))
;  (loop [i 0, sum 0]
;    (when (< i iteration)
;      (let [frame (util/HashMap.)
;            thistime (util/rangei 80 400)
;            offsetNorm (util/rangef 0 1)
;            theta (util/rangef 0 (* 2 math/PI))]
;        (aset frame "time" sum)
;        (aset frame "direction" (vecmath/Vector2d. (* (math/sin theta) (* offsetNorm offsetNorm offsetNorm offsetMax aspect))
;                                  (* (math/cos theta) (* offsetNorm offsetMax))))
;        (.add frames frame)
;        (.addPoint alphaCurve sum (util/ranged 0.4 0.7))
;        (recur (inc i) (+ sum thistime)))))
;  (def int_get
;    (fn []
;      (let [timeInput (mod (* (GameTimer/getAbsTime) 1000) @maxtime)]
;        (first (filter #(> (:time %) timeInput) frames)))))
;
;  ;;
;
;  ;; 绘制普通状态
;  (defn- drawNormal [poverload]
;    (let [tex (if @overlayTexture TEX_BACK_NORMAL TEX_BACK_OVERLOAD)]
;      (ACRenderingHelper/drawTexture tex 0 0 WIDTH HEIGHT 0 0 1 1)
;      (ACRenderingHelper/drawTexture TEX_CP 0 0 (* WIDTH poverload) HEIGHT 0 0 poverload 1)))
;
;  ;; 绘制过载状态
;  (defn- drawOverload [poverload]
;    (let [tex (if @overlayTexture TEX_FRONT_OVERLOAD TEX_OVERLOADED)]
;      (ACRenderingHelper/drawTexture TEX_BACK_NORMAL 0 0 WIDTH HEIGHT 0 0 1 1)
;      (ACRenderingHelper/drawTexture tex 0 0 (* WIDTH poverload) HEIGHT 0 0 poverload 1)))
;
;  ;; 绘制 CP 条
;  (defn- drawCPBar [pcp low]
;    (let [colors (if low overrideColors cpColors)
;          prog (ACRenderingHelper/ProgBar. 0 0 WIDTH HEIGHT colors)]
;      (.setProgress prog pcp)
;      (.draw prog)))
;
;  ;; 绘制预设提示
;  (defn- drawPresetHint [progress time]
;    (let [tex (Resources/getTexture "guis/preset_hint.png")
;          w (tex/getWidth)
;          h (tex/getHeight)
;          x (- WIDTH (* w progress))
;          y (- HEIGHT h)]
;      (ACRenderingHelper/drawTexture tex x y w h 0 0 1 1)
;      (let [font (Resources/font)
;            option (ACRenderingHelper/FontOption. 20)]
;        (.setColor option (ACRenderingHelper/Color. 1 1 1 (* 0.5 (math/sin (* 0.01 time)) 0.5)))
;        (.draw font "Preset" (+ x 10) (+ y 10) option)
;        (.draw font "Switched" (+ x 10) (+ y 30) option))))
;
;  ;; 绘制数据
;  (defn- drawData [cpData time]
;    (let [alpha (if (cpData/isOverloaded) 0.0
;                                          (let [dt (if @showingNumbers (- (- time @lastShowValueChange) 200) Long/MAX_VALUE)]
;                                            (math/clamp 0 1 (/ (- dt 200) 400.0))))
;          x0 110
;          font (Resources/font)
;          option (ACRenderingHelper/FontOption. 40)]
;      (.setAlpha option (* 0.6 @mAlpha alpha))
;      (.setColor option (ACRenderingHelper/Color. 1 1 1))
;      (.draw font "CP " x0 50 option)
;      (.draw font (str (math/floor (cpData/getCP))) (+ x0 (.getTextWidth font "CP " option)) 50 option)
;      (.draw font (str "/") (+ x0 (.getTextWidth font (str (math/floor (cpData/getCP))) option)) 50 option)
;      (.draw font (str (math/floor (cpData/getMaxCP)))) (+ x0 (.getTextWidth font (str "/") option)) 50 option)
;    (.draw font "OL " x0 90 option)
;    (.draw font (str (math/floor (cpData/getOverload))) (+ x0 (.getTextWidth font "OL " option)) 90 option)
;    (.draw font (str "/") (+ x0 (.getTextWidth font (str (math/floor (cpData/getOverload))) option)) 90 option)
;    (.draw font (str (math/floor (cpData/getMaxOverload)))) (+ x0 (.getTextWidth font (str "/") option)) 90 option)))
;
;(defn- balance [cur target delta]
;  (if (< cur target)
;    (math/min (+ cur delta) target)
;    (math/max (- cur delta) target)))
;
;(defn- checkGLError [msg]
;  (let [err (GL11/glGetError)]
;    (when (not= err GL11/GL_NO_ERROR)
;      (throw (Exception. (str "GL Error: " msg " " err))))))
;
;(defn- initEvents []
;  ;; 监听帧事件
;  (event/listen instance FrameEvent (fn [w e]
;                                      (let [player (Minecraft/getMinecraft).player
;                                            cpData (DataPart/get player DataPart/CPData)
;                                            aData (DataPart/get player DataPart/AbilityData)]
;                                        (when (aData/hasCategory)
;                                          (let [c (aData/getCategory)]
;                                            (reset! overlayTexture (.getOverlayIcon c)))))
;                                      (let [active (-> (DataPart/get (Minecraft/getMinecraft).player DataPart/CPData)
;                                                     .isActivated)
;                                            time (long (* (GameTimer/getTime) 1000))]
;                                        (when (and (not @lastFrameActive) active)
;                                          (reset! showTime time)))
;                                      (let [time (long (* (GameTimer/getTime) 1000))
;                                            deltaTime (min 100 (- time @lastDrawTime))
;                                            mAlpha (if (and (not @lastFrameActive) (-> (DataPart/get (Minecraft/getMinecraft).player DataPart/CPData)
;                                                                                     .isActivated))
;                                                     (math/min 1.0 (/ (- time @showTime) 200.0))
;                                                     (if (-> (DataPart/get (Minecraft/getMinecraft).player DataPart/CPData)
;                                                           .isActivated
;                                                           (not= true))
;                                                       (math/max 0.0 (- 1.0 (/ (- time @lastDrawTime) 200.0)))
;                                                       0.0))
;                                            cpData (DataPart/get (Minecraft/getMinecraft).player DataPart/CPData)
;                                            bufferedOverload (balance @bufferedOverload (/ (.getOverload cpData) (.getMaxOverload cpData)) (* deltaTime 1E-3 O_BALANCE_SPEED))
;                                            bufferedCP (balance @bufferedCP (/ (.getCP cpData) (.getMaxCP cpData)) (* deltaTime 1E-3 CP_BALANCE_SPEED))
;                                            chProvider @chProvider
;                                            showingNumbers @showingNumbers
;                                            lastShowValueChange @lastShowValueChange
;                                            lastPresetTime @lastPresetTime
;                                            presetChangeTime @presetChangeTime
;                                            active (-> cpData
;                                                     .isActivated)
;                                            interf (-> cpData
;                                                     .isInterfering)
;                                            overloadRecovering (-> cpData
;                                                                 .isOverloadRecovering)]
;                                        (when interf
;                                          (let [frame (int-get)]
;                                            (GL11/glTranslated (.x (.direction frame))
;                                              (.y (.direction frame))
;                                              0)
;                                            (let [timeInput (long (* (GameTimer/getAbsTime) 1000)) maxtime 10000
;                                                  timeInput (-> timeInput (/ 10) (* 10))
;                                                  mAlpha (* mAlpha (.valueAt (alphaCurve) timeInput))]
;                                              )))
;                                        (GL11/glPushMatrix) ; PUSH 1
;                                        (let [poverload (if (> mAlpha 0) (/ (.getOverload cpData) (.getMaxOverload cpData)) 0)
;                                              bufferedOverload (balance bufferedOverload poverload (* deltaTime 1E-3 O_BALANCE_SPEED))
;                                              pcp (if (> mAlpha 0) (/ (.getCP cpData) (.getMaxCP cpData)) 0)
;                                              bufferedCP (balance bufferedCP pcp (* deltaTime 1E-3 CP_BALANCE_SPEED))]
;                                          (when (> mAlpha 0)
;                                            (checkGLError "DrawCPBar")
;                                            (if-not (.isOverloaded cpData)
;                                              (drawNormal bufferedOverload) ; 绘制正常状态的过载条
;                                              (drawOverload bufferedOverload)) ; 绘制过载状态的过载条
;                                            (when (and chProvider (not (.alive chProvider)))
;                                              (reset! chProvider nil))
;                                            (let [estmCons (getConsumptionHint) ; 获取能力消耗提示
;                                                  low (or interf overloadRecovering)] ; 是否处于干扰或过载恢复状态
;                                              (if-not (zero? estmCons)
;                                                (let [ncp (math/max 0 (- (.getCP cpData) estmCons))
;                                                      oldAlpha mAlpha
;                                                      mAlpha (* mAlpha (+ 0.2 (* 0.1 (+ 1 (math/sin (/ time 80.0)))))] ; 闪烁效果
;                                                  (drawCPBar pcp low) ; 绘制当前状态的CP条
;                                                  (reset! mAlpha oldAlpha)
;                                                  (drawCPBar (/ ncp (.getMaxCP cpData)) low)) ; 绘制预测状态的CP条
;                                                (drawCPBar bufferedCP low)))) ; 绘制当前状态的CP条


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;
;(ns cn.academy.client.auxgui.CPBar
;  (:import [cn.academy.ability Category]
;           [cn.academy.ability.context ClientRuntime ContextManager IConsumptionProvider]
;           [cn.academy.datapart AbilityData CPData PresetData]
;           [cn.academy.event.ability PresetSwitchEvent]
;           [cn.academy.AcademyCraft]
;           [cn.academy.client.render.util ACRenderingHelper]
;           [cn.academy.Resources]
;           [cn.lambdalib2.cgui Widget]
;           [cn.lambdalib2.cgui.component DrawTexture]
;           [cn.lambdalib2.cgui.component.Transform WidthAlign]
;           [cn.lambdalib2.cgui.event FrameEvent]
;           [cn.lambdalib2.registry StateEventCallback]
;           [cn.lambdalib2.render.legacy LegacyShaderProgram Tessellator]
;           [cn.lambdalib2.util Colors GameTimer MathUtils RandUtils]
;           [cn.lambdalib2.vis.curve CubicCurve]
;           [net.minecraftforge.fml.common.event FMLInitializationEvent]
;           [net.minecraftforge.fml.common.eventhandler SubscribeEvent]
;           [net.minecraftforge.fml.relauncher Side]
;           [net.minecraft.client Minecraft]
;           [net.minecraft.entity.player EntityPlayer]
;           [net.minecraft.util ResourceLocation]
;           [org.lwjgl.opengl GL11 GL13 GL20 Color Vector2d]))
;
;(def instance (Widget.))
;
;(def WIDTH 964)
;(def HEIGHT 147)
;(def SCALE 0.2)
;
;(def CP_BALANCE_SPEED 2.0)
;(def O_BALANCE_SPEED 2.0)
;
;(def sin41 (Math/sin (Math/toRadians 44.0)))
;
;(def chProvider (atom nil))
;
;(defn init [ev]
;  (let [instance (Widget.)
;        texture (DrawTexture.)
;        texture (.setTex texture (Resources/getTexture "guis/edit_preview/cpbar"))]
;    (.size instance WIDTH HEIGHT)
;    (.scale instance SCALE)
;    (.walign instance WidthAlign/RIGHT)
;    (.addComponent instance texture)
;    (ACRenderingHelper/addElement ACHud/instance instance (fn [] true) "cpbar")))
;
;(defn setHintProvider [provider]
;  (reset! chProvider provider))
;
;(def TEX_BACK_NORMAL (ResourceLocation. "academy:textures/guis/cpbar/back_normal.png"))
;(def TEX_BACK_OVERLOAD (ResourceLocation. "academy:textures/guis/cpbar/back_overload.png"))
;(def TEX_CP (ResourceLocation. "academy:textures/guis/cpbar/cp.png"))
;(def TEX_FRONT_OVERLOAD (ResourceLocation. "academy:textures/guis/cpbar/front_overload.png"))
;(def TEX_OVERLOADED (ResourceLocation. "academy:textures/guis/cpbar/overloaded.png"))
;(def TEX_OVERLOAD_HIGHLIGHT (ResourceLocation. "academy:textures/guis/cpbar/overload_highlight.png"))
;(def TEX_MASK (ResourceLocation. "academy:textures/guis/cpbar/mask.png"))
;
;(def cpColors (atom []))
;(def overrideColors (atom []))
;
;(defn- init-colors []
;  (.add cpColors (ProgColor. 0.0 (Colors/fromHexColor 0xfff06767)))
;  (.add cpColors (ProgColor. 0.35 (Colors/fromHexColor 0xffffae44)))
;  (.add cpColors (ProgColor. 1.0 (Colors/fromHexColor 0xffffffff)))
;  (.add overrideColors (ProgColor. 0.0 (Colors/fromHexColor 0x0Adfdfdf)))
;  (.add overrideColors (ProgColor. 0.55 (Colors/fromHexColor 0x23f0d49d)))
;  (.add overrideColors (ProgColor. 1.0 (Colors/fromHexColor 0x50f56464))))
;
;(init-colors)
;
;(defn- balance [cur target delta]
;  (let [diff (- target cur)]
;    (if (< (Math/abs diff) delta)
;      target
;      (+ cur (* delta (Math/signum diff))))))
;
;(defn- getConsumptionHint []
;  (let [cpData (CPData/get (Minecraft/getMinecraft).player)]
;    (if (not @chProvider)
;      0
;      (let [hint (.getConsumption @chProvider)]
;        (if (and (not (nil? hint)) (not (zero? hint)))
;          hint
;          0))))))
;
;(defn- drawNormal [bufferedOverload]
;  (let [alpha (* mAlpha (Math/max 0 (- 1 bufferedOverload)))]
;    (ACRenderingHelper/drawTexture TEX_BACK_NORMAL 0 0 WIDTH HEIGHT alpha)
;    (ACRenderingHelper/drawTexture TEX_CP 0 0 WIDTH HEIGHT alpha)))
;
;(defn- drawOverload [bufferedOverload]
;  (let [alpha (* mAlpha bufferedOverload)]
;    (ACRenderingHelper/drawTexture TEX_BACK_OVERLOAD 0 0 WIDTH HEIGHT alpha)
;    (ACRenderingHelper/drawTexture TEX_FRONT_OVERLOAD 0 0 WIDTH HEIGHT alpha)
;    (ACRenderingHelper/drawTexture TEX_OVERLOADED 0 0 WIDTH HEIGHT alpha)
;    (ACRenderingHelper/drawTexture TEX_OVERLOAD_HIGHLIGHT 0 0 WIDTH HEIGHT alpha)))
;
;(defn- drawCPBar [bufferedCP low]
;  (let [alpha (* mAlpha (if low 0.5 1.0))]
;    (ACRenderingHelper/drawTexture TEX_MASK 0 0 WIDTH HEIGHT alpha)
;    (let [prog (ACRenderingHelper/interpolateProgColors @cpColors bufferedCP)]
;      (ACRenderingHelper/drawColoredRect 0 0 WIDTH HEIGHT prog alpha))))
;
;
;(defn- drawPresetHint [prog time]
;  (let [alpha (* mAlpha (if (< time 1000) 1.0 (- 1 (/ (- time 1000) 1000.0)))) ; 1s fade-in, 1s stay, 1s fade-out
;        x (- WIDTH (* 0.5 WIDTH) (* 0.5 WIDTH prog))
;        y (* HEIGHT 0.5)]
;    (ACRenderingHelper/drawTexture (PresetData/getIcon) x y (* HEIGHT 0.5) (* HEIGHT 0.5) alpha)))
;
;(defn- drawData [cpData]
;  (let [alpha (if cpData.overloaded 0.0
;                                    (let [dt (if (zero? lastShowValueChange) Long/MAX_VALUE (- (long (* 1000 (- (GameTimer/getTime) lastShowValueChange))) 200))]
;                                      (if showingNumbers
;                                        (MathUtils/clampf 0 1 (/ (- dt 200) 400.0))
;                                        (if (< dt 300) (- 1 (/ dt 300.0)) 0.0))))]
;    (when (> alpha 0)
;      (let [font (Resources/font)
;            option (FontOption. 40)]
;        (.setAlpha (.color option) (Colors/f2i (* 0.6 mAlpha alpha)))
;        (let [str10 "CP "
;              str11 (format "%.0f" cpData.cp)
;              str12 (format "/%.0f" cpData.maxCP)
;              str20 "OL "
;              str21 (format "%.0f" cpData.overload)
;              str22 (format "/%.0f" cpData.maxOverload)
;              len10 (.getTextWidth font str10 option)
;              len11 (.getTextWidth font str11 option)
;              len20 (.getTextWidth font str20 option)
;              len21 (.getTextWidth font str21 option)]
;          (.draw font str10 (- (* 0.5 WIDTH) len10) (* HEIGHT 0.5) option)
;          (.draw font str11 (- (* 0.5 WIDTH) len11) (* HEIGHT 0.5) option)
;          (.draw font str12 (- (* 0.5 WIDTH) len10 len11) (* HEIGHT 0.5) option)
;          (.draw font str20 (- (* 0.5 WIDTH) len20) (- (* HEIGHT 0.5) 40) option)
;          (.draw font str21 (- (* 0.5 WIDTH) len21) (- (* HEIGHT 0.5) 40) option)
;          (.draw font str22 (- (* 0.5 WIDTH) len20 len21) (- (* HEIGHT 0.5) 40) option))))))
;
;(defn- drawInterference []
;  (let [aspect (/ WIDTH HEIGHT)
;        offsetMax 9
;        iteration 60
;        alphaCurve (CubicCurve.)
;        frames (atom [])
;        sum 0]
;    (.addPoint alphaCurve 0 (RandUtils/ranged 0.2 0.8))
;    (dotimes [i iteration]
;      (let [frame (Vector2d.)]
;        (let [thistime (RandUtils/rangei 80 400)
;              offsetNorm (RandUtils/rangef 0 1)
;              theta (RandUtils/rangef 0 (* MathUtils/PI_F 2))]
;          (set! offsetNorm (* offsetNorm offsetNorm offsetNorm))
;          (set! sum (+ sum thistime))
;          (.setTime frame sum)
;          (.setDirection frame (Vector2d. (* (Math/sin theta) (* offsetNorm offsetMax aspect))
;                                 (* (Math/cos theta) (* offsetNorm offsetMax))))
;          (.add frames frame)
;          (.addPoint alphaCurve sum (RandUtils/ranged 0.2 0.8)))))
;    (let [timeInput (long (* 1000 (GameTimer/getAbsTime))) maxtime 10000
;          timeInput (mod timeInput maxtime)
;          timeInput (* (/ timeInput 10) 10)
;          alpha (.valueAt alphaCurve timeInput)]
;      (GL11/glTranslated (.x (.get (rand frames))) (.y (.get (rand frames))) 0)
;      (set! mAlpha (* mAlpha alpha))
;      (drawCPBar (max 0 (- cpData.cp (getConsumptionHint))) true)
;      (set! mAlpha pre_mAlpha))))
;
;(defn- drawData []
;  (let [dt (if (zero? lastShowValueChange) Long/MAX_VALUE (- (long (* 1000 (GameTimer/getTime))) lastShowValueChange))
;        alpha (if (cpData/overloaded?) 0.0
;                                       (if showingNumbers
;                                         (MathUtils/clampf 0 1 (/ (- dt 200) 400.0))
;                                         (if (< dt 300.0) (- 1 (/ dt 300.0)) 0.0)))]
;    (when (> alpha 0)
;      (let [x0 110
;            font Resources/font
;            option (FontOption. 40)]
;        (.setAlpha (.color option) (Colors/f2i (* 0.6 mAlpha alpha)))
;        (let [str10 "CP "
;              str11 (format "%.0f" cpData.cp)
;              str12 (format "/%.0f" cpData.maxCP)
;              str20 "OL "
;              str21 (format "%.0f" cpData.overload)
;              str22 (format "/%.0f" cpData.maxOverload)
;              len10 (.getTextWidth font str10 option)
;              len11 (.getTextWidth font str11 option)
;              len20 (.getTextWidth font str20 option)
;              len21 (.getTextWidth font str21 option)]
;          (.draw font str10 (- (* 0.5 WIDTH) len10) (* HEIGHT 0.5) option)
;          (.draw font str11 (- (* 0.5 WIDTH) len11) (* HEIGHT 0.5) option)
;          (.draw font str12 (- (* 0.5 WIDTH) len10 len11) (* HEIGHT 0.5) option)
;          (.draw font str20 (- (* 0.5 WIDTH) len20) (- (* HEIGHT 0.5) 40) option)
;          (.draw font str21 (- (* 0.5 WIDTH) len21) (- (* HEIGHT 0.5) 40) option)
;          (.draw font str22 (- (* 0.5 WIDTH) len20 len21) (- (* HEIGHT 0.5) 40) option))))))
;
;(defn- drawCPBar [prog cantuse]
;  (let [pre_mAlpha mAlpha]
;    (when cantuse
;      (set! mAlpha (* mAlpha 0.3)))
;    (autoLerp cpColors prog)
;    (set! prog (+ 0.16 (* prog 0.8)))
;    (let [OFF (* 103 sin41)
;          X0 47
;          Y0 30
;          WIDTH 883
;          HEIGHT 84
;          t Tessellator/instance
;          len (* WIDTH prog)
;          len2 (- len OFF)]
;      (GL11/glPushMatrix)
;      (GL11/glTranslated X0 Y0 0)
;      (GL11/glScaled len2 HEIGHT 1)
;      (GL11/glTranslated 0.5 0.5 0)
;      (GL11/glColor4d 1 1 1 mAlpha)
;      (RenderUtils/loadTexture TEX_CPBAR)
;      (.startDrawingQuads t)
;      (.addVertex t 0 0 0)
;      (.addVertex t 0 1 0)
;      (.addVertex t 1 1 0)
;      (.addVertex t 1 0 0)
;      (.draw t)
;      (GL11/glPopMatrix)
;      (set! mAlpha pre_mAlpha)))))
;
;(defn- drawNormal [overload]
;  (RenderUtils/loadTexture TEX_BACK_NORMAL)
;  (color4d 1 1 1 0.8)
;  (HudUtils/rect WIDTH HEIGHT)
;  (let [len (* overload WIDTH)
;        X0 0
;        Y0 21
;        WIDTH 943
;        HEIGHT 104]
;    (autoLerp overrideColors overload)
;    (RenderUtils/loadTexture TEX_MASK)
;    (subHud (- (+ X0 WIDTH) len) Y0 len HEIGHT))))
;
;(defn- drawOverload [overload]
;  (color4d 1 1 1 0.8)
;  (RenderUtils/loadTexture TEX_BACK_OVERLOAD)
;  (HudUtils/rect WIDTH HEIGHT)
;  (color4d 1 1 1 1)
;  (when shaderLoaded
;    (shaderOverloaded/useProgram)
;    (shaderOverloaded/updateTexOffset (/ (GameTimer/getTime) 10000.0)))
;  (GL13/glActiveTexture (+ GL13/GL_TEXTURE0 4))
;  (let [texture4Binding (GL11/glGetInteger GL11/GL_TEXTURE_BINDING_2D)]
;    (GL11/glEnable GL11/GL_TEXTURE_2D)
;    (RenderUtils/loadTexture TEX_MASK)
;    (GL13/glActiveTexture GL13/GL_TEXTURE0)
;    (RenderUtils/loadTexture TEX_FRONT_OVERLOAD)
;    (let [x0 30
;          width2 (- WIDTH x0 20)]
;      (HudUtils/rect x0 0 0 0 width2 HEIGHT width2 HEIGHT))
;    (GL13/glActiveTexture (+ GL13/GL_TEXTURE0 4))
;    (GL11/glBindTexture GL11/GL_TEXTURE_2D texture4Binding)
;    (GL13/glActiveTexture GL13/GL_TEXTURE0)
;    (GL20/glUseProgram 0))
;  (color4d 1 1 1 (+ 0.3 (* 0.35 (+ (Math/sin (/ (GameTimer/getTime) 200.0)) 1))))
;  (RenderUtils/loadTexture TEX_OVERLOAD_HIGHLIGHT)
;  (HudUtils/rect WIDTH HEIGHT))
;
;(defn- initEvents []
;  (listen FrameEvent (fn [w e]
;                       (let [player (.player Minecraft/getMinecraft)
;                             cpData (CPData/get player)
;                             aData (AbilityData/get player)]
;                         (when (.hasCategory aData)
;                           (let [c (.getCategory aData)
;                                 active (.isActivated cpData)
;                                 time (* (GameTimer/getTime) 1000)
;                                 interf (.isInterfering cpData)
;                                 overloadRecovering (.isOverloadRecovering cpData)
;                                 deltaTime (Math/min 100 time (- time lastDrawTime))
;                                 BLENDIN_TIME 200
;                                 showTime (if (and (not lastFrameActive) active) time showTime)
;                                 deltaTime (Math/min 100L time lastDrawTime)
;                                 mAlpha (if (< (- time showTime) BLENDIN_TIME)
;                                          (/ (- time showTime) BLENDIN_TIME)
;                                          (if (< (- time showTime) 1000)
;                                            1
;                                            (if (< (- time showTime) 1200)
;                                              (- 1 (/ (- time showTime) 200))
;                                              0)))
;                                 overload (.getOverload cpData)
;                                 consumptionHint (getConsumptionHint)
;                                 cantuse (or interf overloadRecovering)
;                                 overrideColors (if interf
;                                                  (list (ProgColor. 0.0 CRL_CP_INTERF_BACK)
;                                                    (ProgColor. 1.0 CRL_CP_INTERF_FORE))
;                                                  (if overloadRecovering
;                                                    (list (ProgColor. 0.0 CRL_CP_OVERLOAD_BACK)
;                                                      (ProgColor. 1.0 CRL_CP_OVERLOAD_FORE))
;                                                    (list (ProgColor. 0.0 CRL_CP_BACK)
;                                                      (ProgColor. 1.0 CRL_CP_FORE))))
;                                 cpColors (list (ProgColor. 0.0 CRL_CP_BACK)
;                                            (ProgColor. 0.2 CRL_CP_BACK)
;                                            (ProgColor. 0.8 CRL_CP_FORE)
;                                            (ProgColor. 1.0 CRL_CP_FORE))]
;                             _ (drawPresetHint (/ (- time showTime) 1000.0) (- time showTime))
;                             _ (drawActivateKeyHint)
;                             _ (drawCPBar overload cantuse)
;                             _ (if (> overload 0.8)
;                                 (drawOverload overload)
;                                 (drawNormal overload))
;                             _ (when active
;                                 (lastDrawTime time))
;                             _ (lastFrameActive active)
;                             _ (checkGLError "FrameEvent")))))))
;(initEvents)
