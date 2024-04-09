;(ns cn.academy.energy.client.ui
;  (:import [cn.academy.block.container ContainerMatrix]
;           [cn.academy.block.tileentity TileMatrix]
;           [cn.academy.core.client.ui.TechUI ContainerUI]
;           [cn.academy.energy.api WirelessHelper]
;           [cn.academy.event.energy ChangePassEvent CreateNetworkEvent]
;           [cn.academy.core.client.ui]
;           [cn.lambdalib2.cgui Widget]
;           [cn.lambdalib2.cgui.component TextBox]
;           [cn.lambdalib2.s11n SerializeNullable SerializeStrategy]
;           [cn.lambdalib2.s11n.network Future NetworkMessage NetworkS11n]
;           [net.minecraftforge.fml.relauncher Side]
;           [net.minecraft.client Minecraft]
;           [net.minecraft.entity.player EntityPlayer]
;           [net.minecraftforge.common MinecraftForge]
;           [cn.lambdalib2.registry StateEventCallback]
;           [net.minecraftforge.fml.common.event FMLInitializationEvent]))
;
;(defn GuiMatrix2 [container]
;  "创建一个矩阵界面"
;  (let [tile (.tile container)
;        thePlayer (.player Minecraft)
;        isPlacer (= (.getPlacerName tile) (.getName thePlayer))
;        invPage (InventoryPage "matrix")
;        ret (ContainerUI container invPage)]
;    (defn rebuildInfo [data]
;      "重建信息"
;      (.reset ret.infoPage)
;      (.histogram ret.infoPage
;        (TechUI/histCapacity (fn [] (.load data)) (.getCapacity tile)))
;      (.seplineInfo ret.infoPage)
;      (.property ret.infoPage "owner" (.getPlacerName tile))
;      (.property ret.infoPage "range" (str (.getRange tile)))
;      (.property ret.infoPage "bandwidth" (str (.getBandwidth tile) " IF/T"))
;      (if (.init data)
;        (do
;          (.sepline ret.infoPage "wireless_info")
;          (if isPlacer
;            (do
;              (.property ret.infoPage "ssid" (.ssid data) (fn [newSSID] (send MSG_CHANGE_SSID tile thePlayer newSSID))) ; 显示当前的ssid，并且可以修改
;              (.sepline ret.infoPage "change_pass")
;              (.property ret.infoPage "password" (.pass data) :password true (fn [newPass] (send MSG_CHANGE_PASSWORD tile thePlayer newPass)))) ; 显示当前的密码，并且可以修改
;            (do
;              (.property ret.infoPage "ssid" (.ssid data))
;              (.property ret.infoPage "password" (.pass data) :password true)))
;          )
;        (let [ssidCell (into-array [nil])
;              passwordCell (into-array [nil])]
;          (if isPlacer
;            (do
;              (.sepline ret.infoPage "wireless_init")
;              (.property ret.infoPage "ssid" "" (fn [_] nil) :contentCell ssidCell :colorChange false) ; 显示当前的ssid，并且可以修改
;              (.property ret.infoPage "password" "" (fn [_] nil) :password true :contentCell passwordCell :colorChange false) ; 显示当前的密码，并且可以修改
;              (.blank ret.infoPage 1)
;              (.button ret.infoPage "INIT" (fn [] (let [ssidBox (aget ssidCell 0)
;                                                        passBox (aget passwordCell 0)]
;                                                    (send MSG_INIT tile (.content ssidBox) (.content passBox) (Future/create2 (fn [_] (send MSG_GATHER_INFO tile (Future/create2 (fn [inf] (rebuildInfo inf))))))))))) ; 初始化按钮，点击后会初始化网络
;            (.sepline ret.infoPage "wireless_noinit"))))
;      (send MSG_GATHER_INFO tile (Future/create2 (fn [inf] (rebuildInfo inf))))
;      ret))
;
;  (defclass InitData []
;    [@SerializeNullable ssid
;     @SerializeNullable pass
;     load])
;
;  (defn MatrixNetProxy []
;    "矩阵网络代理"
;    (defn __init [ev]
;      (.addDirectInstance NetworkS11n MatrixNetProxy))
;    (def MSG_GATHER_INFO "gather") ; 消息名
;    (def MSG_INIT "init") ; 消息名
;    (def MSG_CHANGE_PASSWORD "pass") ; 消息名
;    (def MSG_CHANGE_SSID "ssid") ; 消息名
;    (defn gatherInfo [matrix future]
;      "收集信息"
;      (let [optNetwork (WirelessHelper/getWirelessNet matrix)
;            result (InitData.)]
;        (when optNetwork
;          (do
;            (.ssid result (.getSSID optNetwork))
;            (.pass result (.getPassword optNetwork))
;            (.load result (.getLoad optNetwork))))
;        (.sendResult future result)))
;    (defn init [matrix ssid pwd fut]
;      "初始化"
;      (.post MinecraftForge (CreateNetworkEvent. matrix ssid pwd))
;      (.sendResult fut true))
;    (defn changePassword [matrix player pwd]
;      "修改密码"
;      (when (= (.getPlacerName matrix) (.getName player))
;        (.post MinecraftForge (ChangePassEvent. matrix pwd))))
;    (defn changeSSID [matrix player newSSID]
;      "修改SSID"
;      (when (= (.getPlacerName matrix) (.getName player))
;        (when-let [net (WirelessHelper/getWirelessNet matrix)]
;          (.setSSID net newSSID))))
;    (.addListener NetworkS11n MatrixNetProxy MSG_GATHER_INFO (fn [matrix future] (gatherInfo matrix future)) (into-array [Side/SERVER])) ; 添加监听器
;    (.addListener NetworkS11n MatrixNetProxy MSG_INIT (fn [matrix ssid pwd fut] (init matrix ssid pwd fut)) (into-array [Side/SERVER])) ; 添加监听器
;    (.addListener NetworkS11n MatrixNetProxy MSG_CHANGE_PASSWORD (fn [matrix player pwd] (changePassword matrix player pwd)) (into-array [Side/SERVER])) ; 添加监听器
;    (.addListener NetworkS11n MatrixNetProxy MSG_CHANGE_SSID (fn [matrix player newSSID] (changeSSID matrix player newSSID)) (into-array [Side/SERVER])) ; 添加监听器
;    (.addStateEventCallback NetworkS11n MatrixNetProxy "__init" (fn [ev] (__init ev)))) ; 添加状态事件回调
