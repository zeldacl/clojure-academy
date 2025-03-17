;(ns cn.academy.core.client.ui
;  (:import [java.util.function Consumer])
;  (:require [cn.academy.core.Resources :as Resources]
;            [cn.academy.core.client.ui.TechUI :as TechUI]
;            [cn.academy.energy.api.WirelessHelper :as WirelessHelper]
;            [cn.academy.energy.api.block [IWirelessMatrix IWirelessNode IWirelessTile IWirelessUser]]
;            [cn.academy.event.energy [LinkNodeEvent LinkUserEvent UnlinkNodeEvent UnlinkUserEvent]]
;            [cn.academy.energy.impl [NodeConn WirelessNet]]
;            [cn.academy.util LocalHelper]
;            [cn.lambdalib2.cgui [CGuiScreenContainer ScalaCGUI Widget]]
;            [cn.lambdalib2.cgui.component [DrawTexture ProgressBar TextBox]]
;            [cn.lambdalib2.cgui.component.Transform [HeightAlign WidthAlign]]
;            [cn.lambdalib2.cgui.event [FrameEvent GainFocusEvent LeftClickEvent LostFocusEvent]]
;            [cn.lambdalib2.cgui.loader CGUIDocument]
;            [cn.lambdalib2.registry StateEventCallback]
;            [cn.lambdalib2.render.font.IFont [FontAlign FontOption]]
;            [cn.lambdalib2.s11n [SerializeIncluded SerializeNullable SerializeStrategy]]
;            [cn.lambdalib2.s11n.network [Future NetworkMessage NetworkS11nType]]
;            [net.minecraft.entity.player InventoryPlayer]
;            [net.minecraft.inventory Container Slot]
;            [net.minecraft.tileentity TileEntity]
;            [net.minecraft.util.math BlockPos]
;            [net.minecraft.world World]
;            [net.minecraftforge.common MinecraftForge]
;            [net.minecraftforge.fml.common.event FMLInitializationEvent]
;            [org.lwjgl.opengl GL11]))
;
;(def generic_
;  {:readxml (fn [loc] (CGUIDocument/read (ResourceLocation. (str "academy:guis/rework/" loc ".xml"))))})
;
;(def tech-ui
;  (let [page-button-template (.getWidget (generic_/readxml "pageselect") "main")
;        blend-quad-tex (Resources/getTexture "guis/blend_quad")
;        histogram-tex (Resources/getTexture "guis/histogram")
;        line-tex (Resources/getTexture "guis/line")
;        local (LocalHelper/at "ac.gui.common")
;        local-sep (LocalHelper/subPath local "sep")
;        local-hist (LocalHelper/subPath local "hist")
;        local-property (LocalHelper/subPath local "prop")
;        breathe-alpha
;        (let [time (GameTimer/getTime)
;              sin (* (+ 1 (Math/sin (/ time 0.8))) 0.5)]
;          (float (+ 0.675 (* sin 0.175))))]
;
;    (defn draw-text-box [content option x y & [limit]]
;      "绘制文本框"
;      (let [wmargin 5
;            hmargin 2
;            font (Resources/font)
;            extent (.drawSeperated_Sim font content (if limit limit Float/MAX_VALUE) option)]
;        (glColor4f 0 0 0 0.5)
;        (HudUtils/colorRect (- (* (.align option) (.lenOffset extent))) y (+ (* 2 wmargin) (.width extent)) (+ (* 2 hmargin) (.height extent)))
;        (glColor4f 1 1 1 0.8)
;        (.drawSeperated font content (+ x wmargin) (+ y hmargin) (if limit limit Float/MAX_VALUE) option)
;        (glColor4f 1 1 1 1)))
;
;    (defn breathe [widget]
;      "呼吸效果"
;      (let [tex (.getComponent widget DrawTexture)]
;        (.listens widget FrameEvent (fn [] (.setAlpha (.color tex) (Colors/f2i breathe-alpha))))))
;
;    (defn hist-energy [energy max]
;      "能量直方图"
;      (let [color (Colors/fromHexColor 0xff25c4ff)]
;        {:id (local-hist/get "energy")
;         :color color
;         :value (fn [] (/ energy max))
;         :desc (fn [] (str (format "%.0f IF" energy)))}))
;
;    (defn hist-buffer [energy max]
;      "缓冲区直方图"
;      (let [color (Colors/fromHexColor 0xff25f7ff)]
;        {:id (local-hist/get "buffer")
;         :color color
;         :value (fn [] (/ energy max))
;         :desc (fn [] (str (format "%.0f IF" energy)))}))
;
;    (defn hist-phase-liquid [amt max]
;      "液态物质直方图"
;      (let [color (Colors/fromHexColor 0xff7680de)]
;        {:id (local-hist/get "liquid")
;         :color color
;         :value (fn [] (/ amt max))
;         :desc (fn [] (str (format "%.0f mB" amt)))}))
;
;    (defn hist-capacity [amt max]
;      "容量直方图"
;      (let [color (Colors/fromHexColor 0xffff6c00)]
;        {:id (local-hist/get "capacity")
;         :color color
;         :value (fn [] (/ (float amt) max))
;         :desc (fn [] (str (format "%d/%d" amt max)))}))
;
;    (defn- blend-quad [margin]
;      "混合四边形"
;      (let [color (Colors/monoBlend 0.0 0.5)]
;        (let [widget (Widget. "BlendQuad")
;              :margin margin
;              :color color]
;          (.listens widget FrameEvent
;            (fn []
;              (RenderUtils/loadTexture blend-quad-tex)
;              (RenderUtils/loadTexture blend-quad-tex)
;              (Colors/bindToGL color)
;
;              (let [x 0
;                    y 0
;                    w (.transform widget :width)
;                    h (.transform widget :height)
;                    xs (into-array [(- x margin) x (+ x w) (+ x w margin)])
;                    ys (into-array [(- y margin) y (+ y h) (+ y h margin)])]
;                (glBegin GL_QUADS)
;                (doseq [i (range 3)
;                        j (range 3)]
;                  (let [u (/ i 3.0)
;                        v (/ j 3.0)
;                        step 0.3333333333333333]
;                    (glTexCoord2d u v)
;                    (glVertex2d (aget xs i) (aget ys j))
;                    (glTexCoord2d u (+ v step))
;                    (glVertex2d (aget xs i) (aget ys (inc j)))
;                    (glTexCoord2d (+ u step) (+ v step))
;                    (glVertex2d (aget xs (inc i)) (aget ys (inc j)))
;                    (glTexCoord2d (+ u step) v)
;                    (glVertex2d (aget xs (inc i)) (aget ys j)))))
;              (glEnd)
;
;              (glColor4d 1 1 1 1)
;              (RenderUtils/loadTexture line-tex)
;              (RenderUtils/loadTexture line-tex)
;
;              (let [mrg 3.2]
;                (HudUtils/rect (- mrg) -8.6 (+ w (* 2 mrg)) 12)
;                (HudUtils/rect (- mrg) (- h 2) (+ w (* 2 mrg)) 8)))))))
;    widget))
;(HudUtils/rect (- mrg) (- h 2) (+ w (* 2 mrg)) 8)))))))
;widget))
;
;(defn- blank [height]
;  "空白"
;  (let [widget (Widget.)]
;    (.size widget expectWidth height)
;    (conj elements widget)))
;
;(defn- element [widget]
;  "元素"
;  (let [height (.height widget)]
;    (set! expectHeight (+ expectHeight height))
;    (conj elements widget)))
;
;(defn- hist-property [elem]
;  "直方图属性"
;  (let [widget (new Widget)
;        bar (new Widget)
;        progress (blend (new ProgressBar))]
;    (.size widget 210 210)
;    (.scale widget 0.4)
;    (.addComponent bar (blend (new DrawTexture histogramTex)))
;    (.addComponent bar (blend (new DrawTexture histogram-tex)))
;    (.addComponent progress)
;    (doseq [[idx elem] (map-indexed vector elem)]
;      (.pos bar (+ 56 (* idx 40)) 78)
;      (.color progress (elem :color))
;      (.dir progress Direction/UP)
;      (.listens bar FrameEvent (fn [] (.progress progress (MathUtils/clampd 0.03 1 (elem :value))))))
;    (blank -30)
;    (element widget)
;    (doseq [elem elems]
;      (hist-property elem))
;    this))
;
;(defn- sepline-info []
;  "分隔线信息"
;  (sepline "info"))
;
;(defn- button [name callback]
;  "按钮"
;  (let [textBox (blend (newTextBox (FontOption. 9 FontAlign/CENTER)))
;        len (.getTextWidth (.font textBox) name (.option textBox))
;        widget (new Widget)]
;    (.walign widget WidthAlign/CENTER)
;    (.size widget (max 50 (+ len 5)) 8)
;    (.listens widget FrameEvent
;      (fn []
;        (let [lum (if (.hovering widget) 1.0 0.8)
;              color (.color (.option textBox))]
;          (.setRed color (Colors/f2i lum))
;          (.setGreen color (Colors/f2i lum))
;          (.setBlue color (Colors/f2i lum)))))
;    (.listens widget LeftClickEvent callback)
;    (.addComponent widget textBox)
;    (element widget)))
;
;(defn- kvpair [key value]
;  "键值对"
;  (let [idleColor (Colors/fromHexColor 0xffffffff)
;        editColor (Colors/fromHexColor 0xff2180d8)
;        textBox (blend (newTextBox (FontOption. 8)))
;        valueArea (new Widget)]
;    (.setContent textBox (str value))
;    (.size valueArea 40 8)
;    (.halign valueArea HeightAlign/CENTER)
;    (if-not (nil? editCallback)
;      (.allowEdit textBox true)
;      (.listens valueArea FrameEvent (fn [] (.setContent textBox (str value)))))
;    (if password
;      (.doesEcho textBox true))
;    (.addComponent valueArea textBox)
;    (let [box0 (let [ret (new Widget)]
;                 (.size ret 10 8)
;                 (.halign ret HeightAlign/CENTER)
;                 (.addComponent ret (blend (newTextBox (FontOption. 8)).setContent "[")))
;          box1 (let [ret (new Widget)]
;                 (.size ret 10 8)
;                 (.halign ret HeightAlign/CENTER)
;                 (.addComponent ret (blend (newTextBox (FontOption. 8)).setContent "]")))]
;      (.pos box0 -4 0)
;      (.pos box1 (+ (.width valueArea) 2) 0)
;      (.doesListenKey box0 false)
;      (.doesListenKey box1 false)
;      (.addComponent valueArea box0)
;      (.addComponent valueArea box1)))
;  (let [widget (new Widget)]
;    (.size widget (- expectWidth 10) 8)
;    (.pos widget 6 0)
;    (let [keyArea (new Widget)
;          icon (new Widget)
;          valueArea (new Widget)]
;      (.size keyArea 40 8)
;      (.halign valueArea HeightAlign/CENTER)
;      (.addComponent keyArea (blend (newTextBox (FontOption. 8)).setContent (localProperty/get key)))
;      (.addComponent keyArea (blend (newTextBox (FontOption. 8)).setContent (local-property/get key)))
;      (.pos icon -3 0.5)
;      (.size icon 6 6)
;      (.halign icon HeightAlign/CENTER)
;      (.addComponents icon (blend (new DrawTexture nil).setColor (elem :color)))
;      (.addComponent valueArea (blend (newTextBox (FontOption. 8)).setContent (elem :desc)))
;      (.listens valueArea FrameEvent (fn [] (.setContent (.component valueArea) (elem :desc)))))
;    (.addComponent widget keyArea)
;    (.addComponent widget icon)
;    (.addComponent widget valueArea)
;    (element widget)))
;
;(defn- blank [ht]
;  "空白"
;  (set! elemY (+ elemY (float ht)))
;  this)
;
;(defn- reset []
;  "重置"
;  (doseq [elem elements]
;    (.dispose elem))
;  (elements/clear)
;  (set! elemY 10)
;  (doseq [ua uas]
;    (.clear ua))
;  (set! expectHeight 10)
;  this)
;
;(defn- blend [obj]
;  "混合"
;  (ua/add obj)
;  obj)
;
;(defn hist-energy [energy max]
;  "直方图能量"
;  (let [color (Colors/fromHexColor 0xff25c4ff)]
;    (HistElement (localHist/get "energy") color #(double (/ (energy) max)) #(str (format "%.0f IF" (energy))))))
;
;(defn hist-buffer [energy max]
;  "直方图缓冲"
;  (let [color (Colors/fromHexColor 0xff25f7ff)]
;    (HistElement (localHist/get "buffer") color #(double (/ (energy) max)) #(str (format "%.0f IF" (energy))))))
;
;(defn hist-phase-liquid [amt max]
;  "直方图相位液体"
;  (let [color (Colors/fromHexColor 0xff7680de)]
;    (HistElement (localHist/get "liquid") color #(double (/ (amt) max)) #(str (format "%.0f mB" (amt))))))
;
;(defn hist-capacity [amt max]
;  "直方图容量"
;  (let [color (Colors/fromHexColor 0xffff6c00)]
;    (HistElement (localHist/get "capacity") color #(double (/ (amt) max)) #(str (format "%d/%d" (amt) max))))))
;
;(defn- should-display-inventory [page]
;  "是否显示库存"
;  (= (.id page) "inv"))
;
;(defn- is-slot-active []
;  "是否激活插槽"
;  (should-display-inventory (.currentPage main)))
;
;(defn- find-nodes []
;  "查找节点"
;  (let [msg (new PacketWireless(MSG_FIND_NODES))]
;    (.sendToServer Minecraft/getMinecraft.thePlayer msg)))
;
;(defn- find-networks []
;  "查找网络"
;  (let [msg (new PacketWireless(MSG_FIND_NETWORKS))]
;    (.sendToServer Minecraft/getMinecraft.thePlayer msg)))
;
;(defn- user-connect [user]
;  "用户连接"
;  (let [msg (new PacketWireless(MSG_USER_CONNECT))]
;    (.writeString msg (.ssid user))
;    (.writeBoolean msg (.encrypted user))
;    (.writeBlockPos msg (new BlockPos (.x user) (.y user) (.z user)))
;    (.sendToServer Minecraft/getMinecraft.thePlayer msg)))
;
;(defn- user-disconnect []
;  "用户断开连接"
;  (let [msg (new PacketWireless(MSG_USER_DISCONNECT))]
;    (.sendToServer Minecraft/getMinecraft.thePlayer msg)))
;
;(defn- node-connect [node]
;  "节点连接"
;  (let [msg (new PacketWireless(MSG_NODE_CONNECT))]
;    (.writeBoolean msg (.encrypted node))
;    (.writeBlockPos msg (new BlockPos (.x node) (.y node) (.z node)))
;    (.sendToServer Minecraft/getMinecraft.thePlayer msg)))
;
;(defn- node-disconnect []
;  "节点断开连接"
;  (let [msg (new PacketWireless(MSG_NODE_DISCONNECT))]
;    (.sendToServer Minecraft/getMinecraft.thePlayer msg)))
;
;(defn- update-nodes [result]
;  "更新节点"
;  (let [linked (.linked result)
;        avail (.avail result)]
;    (when-not (nil? linked)
;      (let [tile (.asInstanceOf linked TileEntity)]
;        (when-not (nil? tile)
;          (let [name (.getUserName tile)]
;            (seplineInfo)
;            (button (str (local/get "unlink" name)) #(userDisconnect))
;            (sepline-info)
;            (button (str (local/get "unlink" name)) #(user-disconnect))
;            (kvpair (local/get "connected") (str (local/get "connected_to" name)))
;            (histogram (hist-energy #(get-energy-stored tile) (get-max-energy-stored tile))
;              (hist-buffer #(get-energy-stored tile) (get-max-energy-stored tile))
;              (hist-phase-liquid #(get-liquid-stored tile) (get-max-liquid-stored tile)))
;            (blank 10)))))))
;(doseq [node avail]
;  (let [tile (.asInstanceOf node TileEntity)]
;    (when-not (nil? tile)
;      (let [name (.getNodeName tile)]
;        (seplineInfo)
;        (button (str (local/get "connect" name)) #(nodeConnect node))
;        (sepline-info)
;        (button (str (local/get "connect" name)) #(node-connect node))
;        (kvpair (local/get "available") name)
;        (histogram (hist-energy #(get-energy-stored tile) (get-max-energy-stored tile))
;          (hist-buffer #(get-energy-stored tile) (get-max-energy-stored tile))
;          (hist-phase-liquid #(get-liquid-stored tile) (get-max-liquid-stored tile)))
;        (blank 10)))))))
;
;(defn- update-networks [result]
;  "更新网络"
;  (let [linked (.linked result)
;        avail (.avail result)]
;    (when-not (nil? linked)
;      (let [tile (.asInstanceOf linked TileEntity)]
;        (when-not (nil? tile)
;          (let [name (.getSSID tile)]
;            (seplineInfo)
;            (button (str (local/get "disconnect" name)) #(nodeDisconnect))
;            (kvpair (local/get "connected") (str (local/get "connected_to" name)))
;            (histogram (hist-energy #(get-energy-stored tile) (get-max-energy-stored tile))
;              (hist-buffer #(get-energy-stored tile) (get-max-energy-stored tile))
;              (hist-phase-liquid #(get-liquid-stored tile) (get-max-liquid-stored tile)))
;            (blank 10)))))))
;(doseq [node avail]
;  (let [tile (.asInstanceOf node TileEntity)]
;    (when-not (nil? tile)
;      (let [name (.getSSID tile)]
;        (seplineInfo)
;        (button (str (local/get "connect" name)) #(nodeConnect node))
;        (kvpair (local/get "available") name)
;        (histogram (hist-energy #(get-energy-stored tile) (get-max-energy-stored tile))
;          (hist-buffer #(get-energy-stored tile) (get-max-energy-stored tile))
;          (hist-phase-liquid #(get-liquid-stored tile) (get-max-liquid-stored tile)))
;        (blank 10)))))))
;
;(defn- rebuild-page [window linked avail]
;  "重建页面"
;  (let [wlist (.getWidget window "panel_wireless/zone_elementlist")
;        elist (ElementList.)
;        elemTemplate (.copy (.getWidget wlist "element"))]
;    (.removeComponent wlist (classOf ElementList))
;    (.doesDraw (.getWidget wlist "element").transform false)
;    (when-not (nil? linked)
;      (let [connectElem (.child window "panel_wireless/elem_connected")
;            iconConnect (.component connectElem "icon_connect")
;            iconLogo (.component connectElem "icon_logo")
;            textName (.component connectElem "text_name")]
;        (.setTex iconConnect (Resources/getTexture "guis/icons/icon_connected"))
;        (.setContent textName (str (local/get "connected_to" (.name linked))))
;        (.component iconConnect LinkedInfo/target) linked))
;    (doseq [target avail]
;      (let [instance (.copy elemTemplate)]
;        (let [passBox (.getWidget instance "input_pass")
;              iconKey (.getWidget instance "icon_key")]
;          (when (.encrypted target)
;            (.listens passBox ConfirmInputEvent #(do (target/connect (.content passBox))
;                                                     (.setContent (.component passBox TextBox) "")))
;            (.listens passBox GainFocusEvent #(do (.setAlpha (.color iconKey) (Colors/f2i 1.0f))))
;            (.listens passBox LostFocusEvent #(do (.setAlpha (.color iconKey) (Colors/f2i 0.6f)))))
;          (.listens (.getWidget instance "icon_connect") LeftClickEvent #(target/connect ""))
;          (.setContent (.component (.getWidget instance "text_name") TextBox) (.name target))
;          (when-not (.encrypted target)
;            (doseq [w [passBox iconKey]]
;              (.doesDraw (.transform w) false)))
;          (.addWidget elist instance)))
;      (.addWidget wlist elist)))
;
;  (defn- node-page [node]
;    "节点页面"
;    (let [ret (WirelessPage.)
;          world (.getWorld node)]
;      (defn- rebuild []
;        (let [linked (Option. (WirelessHelper/getWirelessNet node))]
;          (send MSG_FIND_NETWORKS node (Future/create #(updateNetworks %)))
;          (rebuild-page (.window ret) linked (map (fn [net]
;                                                    (let [tile (.asInstanceOf net TileEntity)]
;                                                      (when-not (nil? tile)
;                                                        (let [ssid (.getSSID net)
;                                                              encrypted (not (.getPassword net).isEmpty)]
;                                                          (assoc {} :ssid ssid :encrypted encrypted :tile tile))))) avail))))
;      (rebuild)
;      (.setTex (.child (.window ret) "icon_logo") toMatrixIcon)
;      ret))
;
;  (defn- user-page [user]
;    "用户页面"
;    (let [ret (WirelessPage.)
;          world (.getWorld user)]
;      (defn- rebuild []
;        (let [linked (Option. (WirelessHelper/getNodeConn user))]
;          (send MSG_FIND_NODES user (Future/create #(updateNodes %)))
;          (rebuild-page (.window ret) linked (map (fn [conn]
;                                                    (let [tile (.asInstanceOf conn TileEntity)]
;                                                      (when-not (nil? tile)
;                                                        (let [name (.getNodeName tile)]
;                                                          (assoc {} :name name :encrypted (.encrypted conn) :tile tile))))) avail))))
;      (rebuild)
;      (.setTex (.child (.window ret) "icon_logo") toMatrixIcon)
;      ret))
;
;  (defn- rebuild-page [window linked avail]
;    "重建页面"
;    (let [wlist (.getWidget window "panel_wireless/zone_elementlist")
;          elist (ElementList.)
;          elemTemplate (.copy (.getWidget wlist "element"))]
;      (.removeComponent wlist (classOf ElementList))
;      (.doesDraw (.getWidget wlist "element").transform false)
;      (when-not (nil? linked)
;        (let [connectElem (.child window "panel_wireless/elem_connected")
;              iconConnect (.component connectElem "icon_connect")
;              iconLogo (.component connectElem "icon_logo")
;              textName (.component connectElem "text_name")]
;          (.setTex iconConnect (Resources/getTexture "guis/icons/icon_connected"))
;          (.setContent textName (str (local/get "connected_to" (.name linked))))
;          (.component iconConnect LinkedInfo/target) linked))
;      (doseq [target avail]
;        (let [instance (.copy elemTemplate)]
;          (let [passBox (.getWidget instance "input_pass")
;                iconKey (.getWidget instance "icon_key")]
;            (when (.encrypted target)
;              (.listens passBox ConfirmInputEvent #(do (target/connect (.content passBox))
;                                                       (.setContent (.component passBox TextBox) "")))
;              (.listens passBox GainFocusEvent #(do (.setAlpha (.color iconKey) (Colors/f2i 1.0f))))
;              (.listens passBox LostFocusEvent #(do (.setAlpha (.color iconKey) (Colors/f2i 0.6f)))))
;            (.listens (.getWidget instance "icon_connect") LeftClickEvent #(target/connect ""))
;            (.setContent (.component (.getWidget instance "text_name") TextBox) (.name target))
;            (when-not (.encrypted target)
;              (doseq [w [passBox iconKey]]
;                (.doesDraw (.transform w) false)))
;            (.addWidget elist instance)))
;        (.addWidget wlist elist)))
;
;    (defn- apply []
;      "应用"
;      (let [widget (.copy wirelessPageTemplate)]
;        (.breathe TechUI/local widget)
;        (let [wirelessPanel (.getWidget widget "panel_wireless")
;              wlist (.getWidget wirelessPanel "zone_elementlist")]
;          (.listens (.getWidget wirelessPanel "btn_arrowup") LeftClickEvent #(progressLast elist))
;          (.listens (.getWidget wirelessPanel "btn_arrowdown") LeftClickEvent #(progressNext elist))
;          (let [connectIcon (.child wirelessPanel "elem_connected/icon_connect")]
;            (.addWidget connectIcon (LinkedInfo. nil))
;            (.listens connectIcon LeftClickEvent #(let [target (.component connectIcon LinkedInfo/target)]
;                                                    (when target
;                                                      (.disconnect target)))))
;          (Page. "wireless" widget)))
;
;      (apply)
;      (apply)
