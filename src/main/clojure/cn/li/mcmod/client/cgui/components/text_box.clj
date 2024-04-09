;(ns cn.lambdalib2.cgui.component.TextBox
;  (:import [cn.lambdalib2.cgui.annotation CGuiEditorComponent]
;           [cn.lambdalib2.util ClientUtils GameTimer]
;           [net.minecraft.client.resources I18n]
;           [cn.lambdalib2.cgui Widget]
;           [cn.lambdalib2.s11n SerializeIncluded]
;           [cn.lambdalib2.render.font Fonts IFont FontOption]
;           [cn.lambdalib2.cgui.component.Transform HeightAlign]
;           [org.lwjgl.input Keyboard]
;           [org.lwjgl.opengl GL11]
;           [cn.lambdalib2.cgui.event FrameEvent GuiEvent KeyEvent LeftClickEvent]
;           [net.minecraft.util ChatAllowedCharacters]
;           [org.lwjgl.util.vector Vector2f]))
;
;;; 定义TextBox组件
;@CGuiEditorComponent
;(defcomponent TextBox [content "" font (Fonts/getDefault) option (FontOption.) heightAlign HeightAlign/CENTER localized false allowEdit false emit true doesEcho false echoChar \* zLevel 0 xOffset 0 yOffset 0 caretPos 0 displayOffset 0]
;  ;; 绘制组件
;  (listen FrameEvent (fn [w e]
;                       (validate)
;                       (let [origin (origin)
;                             widthLimit (- (.width (.transform w)) xOffset)
;                             processed (subs (processedContent) displayOffset)
;                             localCaret (- caretPos displayOffset)
;                             acc 0.0f
;                             i (if emit
;                                 (loop [i 0]
;                                   (if (and (< i (count processed)) (< acc widthLimit))
;                                     (recur (inc i))
;                                     i))
;                                 (count processed))
;                             display (subs processed 0 i)]
;                         (GL11/glPushMatrix)
;                         (GL11/glTranslated 0 0 zLevel)
;                         (.draw font display (.x origin) (.y origin) option)
;                         (when (and (.isFocused w) allowEdit (odd? (mod (GameTimer/getAbsTime) 2)))
;                           (.draw font "|" (+ (.x origin) (sumLength display 0 localCaret)) (- (.y origin) 1) option))
;                         (GL11/glPopMatrix))))
;  ;; 处理输入
;  (listen KeyEvent (fn [__ evt]
;                     (when allowEdit
;                       (let [input (.inputChar evt)
;                             keyCode (.keyCode evt)]
;                         (condp = keyCode
;                           Keyboard/KEY_RIGHT
;                           (do
;                             (set! caretPos (min (count content) (inc caretPos)))
;                             (checkCaretRegion))
;                           Keyboard/KEY_LEFT
;                           (do
;                             (set! caretPos (max 0 (dec caretPos)))
;                             (when (< caretPos displayOffset)
;                               (set! displayOffset caretPos)))
;                           (and (= keyCode Keyboard/KEY_V) (.isKeyDown Keyboard/KEY_LCONTROL))
;                           (do
;                             (setContent (str (subs content 0 caretPos) (ClientUtils/getClipboardContent) (subs content caretPos)))
;                             (validate)
;                             (.post widget (ChangeContentEvent)))
;                           (and (= keyCode Keyboard/KEY_C) (.isKeyDown Keyboard/KEY_LCONTROL))
;                           (ClientUtils/setClipboardContent content)
;                           Keyboard/KEY_BACK
;                           (when (not= caretPos 0)
;                             (set! content (str (subs content 0 (dec caretPos)) (subs content caretPos)))
;                             (dec! caretPos)
;                             (when (not= displayOffset 0)
;                               (dec! displayOffset))
;                             (.post widget (ChangeContentEvent))
;                             (checkCaretRegion)
;                             (validate))
;                           (or (= keyCode Keyboard/KEY_RETURN) (= keyCode Keyboard/KEY_NUMPADENTER))
;                           (.post widget (ConfirmInputEvent))
;                           Keyboard/KEY_DELETE
;                           (do
;                             (set! content "")
;                             (.post widget (ChangeContentEvent))
;                             (validate))
;                           (ChatAllowedCharacters/isAllowedCharacter input)
;                           (do
;                             (set! content (str (subs content 0 caretPos) input (subs content caretPos)))
;                             (set! caretPos (min (count content) (inc caretPos)))
;                             (.post widget (ChangeContentEvent))
;                             (checkCaretRegion)))))))
;  ;; 鼠标光标位置选择
;  (listen LeftClickEvent (fn [w evt]
;                           (when allowEdit
;                             (let [origin (origin)
;                                   display (subs (processedContent) displayOffset)
;                                   rel-x (+ (.x origin) (* (.getTextWidth font display option) (.lenOffset (.align option))) (.x evt))
;                                   acc 0.0f
;                                   ind 0]
;                               (loop [ind 0]
;                                 (when (and (< acc rel-x) (< ind (count display)))
;                                   (set! acc (+ acc (.getCharWidth font (.codePointAt display ind) option)))
;                                   (recur (inc ind))))
;                               (when (and (> ind 0) (< rel-x (- acc (* (.getCharWidth font (.codePointAt display (dec ind)) option) 0.5))))
;                                 (dec! ind))
;                               (set! caretPos (+ displayOffset ind))
;                               (checkCaretRegion))))))
;;; 允许编辑
;(defn allowEdit []
;  (set! allowEdit true)
;  this)
;;; 设置内容
;(defn setContent [str]
;  (set! content str)
;  this)
;;; 设置字体
;(defn setFont [font]
;  (set! this.font font)
;  this)
;;; 设置高度对齐方式
;(defn setHeightAlign [align]
;  (set! heightAlign align)
;  this)
;;; 验证
;(defn validate []
;  (when (not allowEdit)
;    (set! displayOffset caretPos 0))
;  (when (or (>= displayOffset (count content)) (> caretPos (count content)))
;    (set! displayOffset caretPos 0)))
;;; 获取起点
;(defn origin []
;  (let [origin-x (* (.width (.transform widget)) (.lenOffset (.align option)) xOffset)
;        origin-y (+ (* (max 0 (- (.height (.transform widget)) (.fontSize option))) (.factor heightAlign) yOffset)]
;    (Vector2f. origin-x origin-y)))
;;; 是否本地化
;(defn shouldLocalize []
;  (and (not allowEdit) localized))
;;; 检查光标区域
;(defn checkCaretRegion []
;  (let [widthLimit (widthLimit)
;        local (subs (processedContent) displayOffset)
;        localCaret (- caretPos displayOffset)
;        distance (sumLength local 0 localCaret)]
;    (when (> distance widthLimit)
;      (let [acc 0.0f
;            mini (loop [mini 0]
;                   (if (and (< mini localCaret) (> (- distance acc) widthLimit))
;                     (recur (inc mini))
;                     mini))]
;        (set! displayOffset (+ displayOffset mini))))
;    (when (>= displayOffset caretPos)
;      (set! displayOffset (max 0 (dec caretPos))))))
;
;;; 处理内容
;(defn processedContent []
;  (let [ret content]
;    (when (shouldLocalize)
;      (set! ret (I18n/format ret)))
;    (when doesEcho
;      (set! ret (apply str (repeat (count ret) echoChar))))
;    ret))
;;; 计算长度
;(defn sumLength [str begin end]
;  (.getTextWidth font (subs str begin end) option)))
;
;;; 获取TextBox组件
;(defn get [w]
;  (.getComponent w TextBox))
;
;public static TextBox get(Widget w) {
;                                     return w.getComponent(TextBox.class);
;                                     }
;
;}
