;(ns cn.academy.client.auxgui
;  (:import [cn.academy.terminal DonatorList App AppEnvironment TerminalData]
;           [cn.academy.util RegACKeyHandler]
;           [cn.lambdalib2.auxgui AuxGui AuxGuiHandler]
;           [cn.lambdalib2.cgui CGui Widget WidgetContainer]
;           [cn.lambdalib2.cgui.component Component DrawTexture TextBox]
;           [cn.lambdalib2.cgui.event FrameEvent]
;           [cn.lambdalib2.cgui.loader CGUIDocument]
;           [cn.lambdalib2.input KeyHandler KeyManager]
;           [cn.lambdalib2.registry StateEventCallback]
;           [cn.lambdalib2.util Colors HudUtils RenderUtils SideUtils]
;           [com.google.common.base Preconditions]
;           [net.minecraft.client Minecraft ScaledResolution]
;           [net.minecraft.entity.player EntityPlayer]
;           [net.minecraft.util ResourceLocation TextComponentTranslation]
;           [net.minecraft.util.math MathHelper]
;           [net.minecraft.util.text.translation I18n]
;           [net.minecraftforge.common MinecraftForge]
;           [net.minecraftforge.fml.common.event FMLInitializationEvent]
;           [net.minecraftforge.fml.common.eventhandler SubscribeEvent]
;           [net.minecraftforge.fml.relauncher Side]
;           [org.lwjgl.input Keyboard Mouse]
;           [org.lwjgl.opengl GL11]
;           [org.lwjgl.util.glu GLU])
;  (:require [cn.lambdalib2.util GameTimer ControlOverrider]))
;
;;; 定义常量
;(def MAX-MX 605)
;(def MAX-MY 740)
;(def SENSITIVITY 0.7)
;(def BALANCE-SPEED 3000)
;
;;; 定义资源路径
;(def APP-BACK (ResourceLocation. "app_back"))
;(def APP-BACK-HDR (ResourceLocation. "app_back_highlight"))
;(def CURSOR (ResourceLocation. "cursor"))
;
;;; 定义全局变量
;(def current nil)
;(def loaded nil)
;
;;; 定义辅助函数
;(defn balance [dt from to]
;  (let [d (- to from)]
;    (+ from (* (min BALANCE-SPEED dt (Math/abs d)) (Math/signum d)))))
;
;
;;; 定义主函数
;(defn terminal-ui []
;  "终端UI"
;  (let [gui (CGui.)
;        root (.addWidget gui (-> (CGUIDocument/read (ResourceLocation. "academy:guis/terminal.xml"))
;                               (.getWidget "back")
;                               (.copy)))
;        helper (TerminalMouseHelper.)
;        old-helper (.mouseHelper Minecraft.getMinecraft)
;        click-handler (LeftClickHandler.)
;        mouse-x (atom 150)
;        mouse-y (atom 150)
;        buff-x (atom 150)
;        buff-y (atom 150)
;        create-time (GameTimer/getTime)
;        last-frame-time (atom 0)
;        selection (atom 0)
;        scroll (atom 0)
;        apps (atom [])]
;    (defn update-app-list [data]
;      "更新应用列表"
;      (doseq [w @apps]
;        (.dispose w))
;      (reset! apps [])
;      (doseq [app (.getInstalledApps data)]
;        (let [w (create-app-widget (count @apps) app)]
;          (.addWidget root w)
;          (swap! apps conj w)))
;      (let [count-text (TextComponentTranslation. "ac.gui.terminal.appcount" (count @apps))
;            current-time (mod (.getWorldTime (-> Minecraft getMinecraft getPlayer)) 24000)
;            hour (quot current-time 1000)
;            minutes (quot (* (mod current-time 1000) 60) 1000)
;            time-text (str (if (< hour 10) (str "0" hour) hour) ":" (if (< minutes 10) (str "0" minutes) minutes))]
;        (-> (.getComponent (.getWidget root "text_appcount") TextBox)
;          (.setContent (str count-text ", " time-text))))
;      (update-position))
;    (defn update-position []
;      "更新位置"
;      (let [START-X 65
;            START-Y 155
;            STEP-X 180
;            STEP-Y 180
;            max (get-max-scroll)]
;        (doseq [w @apps]
;          (.setDoesDraw (.transform w) false))
;        (doseq [i (range (* @scroll 3) (min (+ (* @scroll 3) 9) (count @apps)))]
;          (let [order (- i (* @scroll 3))
;                app (@apps i)]
;            (.setDoesDraw (.transform app) true)
;            (.setX (.transform app) (+ START-X (* STEP-X (mod order 3))))
;            (.setY (.transform app) (+ START-Y (* STEP-Y (quot order 3)))))))
;      (defn init-gui []
;        "初始化GUI"
;        (let [player (-> Minecraft getMinecraft getPlayer)
;              data (.get player TerminalData/get)]
;          (reset! create-time (GameTimer/getTime))
;          (-> root (.removeWidget "text_loading") (.removeWidget "icon_loading"))
;          (update-app-list data)
;          (-> root (.getWidget "text_username") (.getComponent TextBox) (.setContent (.getName player)))
;          (-> root (.getWidget "arrow_up") (.listen FrameEvent (fn [w e] (.setEnabled (.getComponent w) (> @scroll 0)))))
;          (-> root (.getWidget "arrow_down") (.listen FrameEvent (fn [w e] (.setEnabled (.getComponent w) (< @scroll (get-max-scroll)))))))
;        (defn draw [sr]
;          "绘制"
;          (let [mc Minecraft/getMinecraft
;                time (GameTimer/getTime)
;                dt (- time @last-frame-time)
;                aspect (/ (float (.displayWidth mc)) (.displayHeight mc))
;                scale (/ 1.0 310)]
;            (swap! mouse-x + (* SENSITIVITY (.dx helper)))
;            (swap! mouse-y - (* SENSITIVITY (.dy helper)))
;            (swap! mouse-x max 0 min MAX-MX)
;            (swap! mouse-y max 0 min MAX-MY)
;            (swap! buff-x (balance dt @buff-x @mouse-x))
;            (swap! buff-y (balance dt @buff-y @mouse-y))
;            (.setX (.transform (.getWidget root "arrow_up")) 0)
;            (.setX (.transform (.getWidget root "arrow_down")) 0)
;            (swap! mouse-y (fn [y]
;                             (cond (= y 0) (do (swap! scroll dec) 1)
;                                   (= y MAX-MY) (do (swap! scroll inc) (- MAX-MY 1))
;                                   :else y)))
;            ;; 将矩阵模式设置为投影矩阵
;            (GL11/glMatrixMode GL11/GL_PROJECTION)
;            ;; 将当前矩阵压入堆栈
;            (GL11/glPushMatrix)
;            ;; 将当前矩阵设置为单位矩阵
;            (GL11/glLoadIdentity)
;            ;; 设置透视投影
;            (GLU/gluPerspective 50 aspect 1.0 100.0)
;            ;; 将矩阵模式设置为模型视图矩阵
;            (GL11/glMatrixMode GL11/GL_MODELVIEW)
;            ;; 将当前矩阵压入堆栈
;            (GL11/glPushMatrix)
;            ;; 将当前矩阵设置为单位矩阵
;            (GL11/glLoadIdentity)
;            ;; 禁用深度测试
;            (GL11/glDisable GL11/GL_DEPTH_TEST)
;            ;; 禁用alpha测试
;            (GL11/glDisable GL11/GL_ALPHA_TEST)
;            ;; 启用混合
;            (GL11/glEnable GL11/GL_BLEND)
;            ;; 设置混合函数
;            (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)
;            ;; 设置颜色
;            (GL11/glColor4d 1 1 1 1)
;            ;; 平移
;            (GL11/glTranslated (* 0.35 aspect) 1.2 -4)
;            (GL11/glTranslated 1 -1.8 0)
;            ;; 旋转
;            (GL11/glRotated -1.6 0 0 1)
;            (GL11/glRotated (- 18 (* 4 (/ (- @buff-x MAX-MX 2) MAX-MX 2)) (* 1 (Math/sin (/ time 1000.0)))) 0 1 0)
;            (GL11/glRotated (+ 7 (* 4 (/ (- @buff-y MAX-MY 2) MAX-MY 2))) 1 0 0)
;            (GL11/glTranslated -1 1.8 0)
;            ;; 缩放
;            (GL11/glScaled scale (- scale) scale)
;            ;; 绘制GUI
;            (.draw gui @mouse-x @mouse-y)
;            ;; 绘制光标
;            (let [csize (* (if (nil? (get-selected-app)) 1 1.3) (+ 20 (* (Math/sin (/ time 300.0)) 2)))]
;              ;; 加载光标纹理
;              (RenderUtils/loadTexture CURSOR)
;              ;; 设置颜色
;              (GL11/glColor4d 1 1 1 0.4)
;              ;; 平移
;              (GL11/glTranslated 0 0 -2)
;              ;; 禁用alpha测试
;              (GL11/glDisable GL11/GL_ALPHA_TEST)
;              ;; 设置混合函数
;              (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE)
;              ;; 绘制矩形
;              (HudUtils/rect (- csize/2 @buff-x) (- csize/2 @buff-y 120) csize csize)
;;; 设置混合函数
;(GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)))
;;; 将当前矩阵弹出堆栈
;(GL11/glPopMatrix)
;;; 将矩阵模式设置为投影矩阵
;(GL11/glMatrixMode GL11/GL_PROJECTION)
;;; 将当前矩阵弹出堆栈
;(GL11/glPopMatrix)
;;; 将矩阵模式设置为模型视图矩阵
;(GL11/glMatrixMode GL11/GL_MODELVIEW)
;;; 启用深度测试
;(GL11/glEnable GL11/GL_DEPTH_TEST)
;;; 启用alpha测试
;(GL11/glEnable GL11/GL_ALPHA_TEST)
;;; 面剔除
;(GL11/glCullFace GL11/GL_BACK))
;(reset! last-frame-time time)))
;(let [instance (AuxGui.)]
;  (reset! current instance)
;  (reset! loaded (CGUIDocument/read (ResourceLocation. "academy:guis/terminal.xml")))
;  (RegACKeyHandler/dynamic-add-key-handler "terminal_click" RegACKeyManager/MOUSE_LEFT click-handler)
;  (ControlOverrider/override OVERRIDE_GROUP RegACKeyManager/MOUSE_LEFT)
;  (DonatorList/Instance/tryRequest)
;  instance))
