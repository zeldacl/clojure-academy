;(ns cn.lambdalib2.cgui.component.DrawTexture
;  (:import [org.lwjgl.opengl GL11 GL20]
;           [cn.lambdalib2.cgui Widget]
;           [cn.lambdalib2.cgui.annotation CGuiEditorComponent]
;           [cn.lambdalib2.cgui.event FrameEvent]
;           [cn.lambdalib2.util Colors HudUtils]
;           [net.minecraft.util ResourceLocation]
;           [org.lwjgl.util Color]))
;
;;; 定义一个枚举类型，表示深度测试模式
;(def DepthTestMode {:Default 0 :Equals 1})
;
;;; 定义一个绘制纹理的组件
;(defn draw-texture []
;  (let [MISSING (ResourceLocation. "lambdalib2:textures/cgui/missing.png")
;        texture (atom MISSING) ;; 纹理
;        color (atom (Colors/white)) ;; 颜色
;        z-level (atom 0) ;; 深度
;        write-depth (atom true) ;; 是否写入深度缓冲区
;        does-use-uv (atom false) ;; 是否使用UV坐标
;        u (atom 0) ;; UV坐标的x分量
;        v (atom 0) ;; UV坐标的y分量
;        tex-width (atom 0) ;; 纹理的宽度
;        tex-height (atom 0) ;; 纹理的高度
;        depth-test-mode (atom DepthTestMode/:Default) ;; 深度测试模式
;        shader-id (atom 0)] ;; 着色器ID
;    (defn listen-frame-event [w e]
;      ;; 绘制纹理
;      (GL11/glBlendFunc GL_SRC_ALPHA GL_ONE_MINUS_SRC_ALPHA) ;; 混合函数
;      (GL11/glDisable GL_ALPHA_TEST) ;; 禁用alpha测试
;      (GL11/glDepthMask @write-depth) ;; 写入深度缓冲区
;      (GL20/glUseProgram @shader-id) ;; 使用着色器
;      (if (= @depth-test-mode DepthTestMode/:Equals) ;; 判断深度测试模式
;        (do
;          (GL11/glEnable GL_DEPTH_TEST) ;; 启用深度测试
;          (GL11/glDepthFunc GL_EQUAL)) ;; 深度测试函数
;        (if @write-depth
;          (do
;            (GL11/glEnable GL_DEPTH_TEST)
;            (GL11/glDepthFunc GL_ALWAYS))
;          (GL11/glDisable GL_DEPTH_TEST)))
;      (Colors/bindToGL @color) ;; 绑定颜色
;      (if (not= @z-level 0)
;        (do
;          (GL11/glPushMatrix) ;; 压入矩阵堆栈
;          (GL11/glTranslated 0 0 @z-level))) ;; 平移
;      (if (and (not= @texture nil) (not= (.getPath @texture) "<null>"))
;        (do
;          (HudUtils/loadTexture @texture) ;; 加载纹理
;          (if @does-use-uv
;            (HudUtils/rect 0 0 @u @v (.width (.transform w)) (.height (.transform w)) @tex-width @tex-height) ;; 绘制带有UV坐标的矩形
;            (HudUtils/rect 0 0 (.width (.transform w)) (.height (.transform w)))) ;; 绘制矩形
;          (if (not= @z-level 0)
;            (GL11/glPopMatrix)) ;; 弹出矩阵堆栈
;          (GL11/glDisable GL_DEPTH_TEST) ;; 禁用深度测试
;          (GL11/glDepthFunc GL_LEQUAL) ;; 深度测试函数
;          (GL20/glUseProgram 0) ;; 不使用着色器
;          (GL11/glDepthMask true)) ;; 写入深度缓冲区
;        (defn set-shader-id [id] ;; 设置着色器ID
;          (reset! shader-id id))
;        (defn set-tex [t] ;; 设置纹理
;          (reset! texture t)
;          this)
;        (defn set-uv-rect [u v tex-width tex-height] ;; 设置UV坐标矩形
;          (reset! does-use-uv true)
;          (reset! u u)
;          (reset! v v)
;          (reset! tex-width tex-width)
;          (reset! tex-height tex-height)
;          this)
;        (defn set-color [c] ;; 设置颜色
;          (Colors/setColor @color c)
;          this)
;        (defn get [w] ;; 获取组件
;          (.getComponent w draw-texture))
;        (defn constructor
;          ([] (constructor MISSING)) ;; 构造函数
;          ([texture] (constructor texture (Colors/white)))
;          ([name texture color]
;           (listen FrameEvent listen-frame-event)
;           (reset! texture texture)
;           (reset! color color)
;           this))
;        (constructor)))
;    color]
;(listen FrameEvent listen-frame-event)
;(reset! texture texture)
;(reset! color color)
;this))
;(constructor)))
;mponent(draw-texture.class);
;}
