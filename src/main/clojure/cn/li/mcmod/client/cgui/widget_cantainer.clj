;(ns cn.lambdalib2.cgui.WidgetContainer
;  (:import [java.util LinkedList Set Map]
;           [com.google.common.collect HashBiMap ImmutableList]
;           [net.minecraftforge.fml.relauncher Side]
;           [cn.lambdalib2.cgui Widget]))
;
;; 定义WidgetContainer
;(defrecord WidgetContainer [^HashBiMap widgets
;                            ^LinkedList widgetList])
;
;; 未命名的widget的前缀
;(def UNNAMED_PRE "Unnamed ")
;
;; 添加所有的widget
;(defn addAll [this container]
;  (doseq [w (.getDrawList container)]
;    (addWidget w.getName w.copy)))
;
;; 更新widget
;(defn update [this]
;  (doseq [w widgetList]
;    (when (.disposed w)
;      (.remove widgetList w)
;      (.remove (.inverse widgets) w))))
;
;; 重命名widget
;(defn renameWidget [this name newName]
;  (let [w (.remove widgets name)]
;    (when-not w
;      (throw (NullPointerException.)))
;    (.put widgets newName w)))
;
;; 获取所有的entries
;(defn getEntries [this]
;  (.entrySet widgets))
;
;; 添加widget
;(defn addWidget [this & args]
;  (let [name (if (string? (first args)) (first args) (getNextName this))
;        add (if (string? (first args)) (second args) (first args))
;        begin (if (string? (first args)) (third args) (second args))]
;    (if-not (checkInit this name add)
;      false
;      (do
;        (if begin
;          (.addFirst widgetList add)
;          (.add widgetList add))
;        (checkAdded this name add)
;        true))))
;
;
;(defn addWidgetAfter [this add pivot]
;  (addWidgetAfter this (getNextName this) add pivot))
;
;(defn addWidgetAfter [this name add pivot]
;  (let [index (.indexOf widgetList pivot)]
;    (if (neg? index)
;      false
;      (if-not (checkInit this name add)
;        false
;        (do
;          (.add widgetList (+ index 1) add)
;          (checkAdded this name add)
;          true)))))
;
;
;(defn addWidgetBefore [this add pivot]
;  (addWidgetBefore this (getNextName this) add pivot))
;
;(defn addWidgetBefore [this name add pivot]
;  (let [index (.indexOf widgetList pivot)]
;    (if (neg? index)
;      (do
;        (.addFirst widgetList add)
;        (checkAdded this name add)
;        true)
;      (if-not (checkInit this name add)
;        false
;        (do
;          (.add widgetList index add)
;          (checkAdded this name add)
;          true)))))
;
;
;(defn checkInit [this name add]
;  "检查是否初始化"
;  (if (.containsKey widgets name)
;    (let [w (.get widgets name)]
;      (when-not (.disposed w)
;        false)
;      (.remove widgets name)))
;  (when (.containsValue widgets add)
;    (.remove (.inverse widgets) add))
;  (.setDisposed add false)
;  (.setDirty add true)
;  (.put widgets name add)
;  true)
;
;(defn checkAdded [this name add]
;  "检查是否添加"
;  (onWidgetAdded this name add)
;  (.setAbstractParent add this)
;  (.onAdded add))
;
;(defn clear [this]
;  "清空"
;  (.clear widgets)
;  (.clear widgetList))
;
;(defn getWidget [this i]
;  "获取widget"
;  (.get widgetList i))
;
;(defn locate [this w]
;  "定位"
;  (.indexOf widgetList w))
;
;(defn onWidgetAdded [this name w]
;  nil)
;
;(defn getWidget [this name]
;  "获取widget"
;  (let [ind (.indexOf name "/")]
;    (if (neg? ind)
;      (.get widgets name)
;      (if-not (= ind (dec (.length name)))
;        (let [cp (.substring name 0 ind)
;              ep (.substring name (+ ind 1))]
;          (let [w (.get widgets cp)]
;            (when w
;              (.getWidget w ep))))
;        nil))))
;
;(defn hasWidget [this name]
;  "是否有widget"
;  (let [w (getWidget this name)]
;    (and w (not (.disposed w)))))
;
;(defn removeWidget [this name]
;  "移除widget"
;  (let [w (.get widgets name)]
;    (when w
;      (removeWidget this w))))
;
;(defn removeWidget [this w]
;  (.dispose w)
;  (.setGui w nil)
;  (.setParent w nil))
;
;(defn forceRemoveWidget [this w]
;  "强制移除widget"
;  (when (= (.getAbstractParent w) this)
;    (do
;      (.remove widgets (.getName w))
;      (.remove widgetList w)
;      (.setGui w nil)
;      (.setParent w nil))))
;
;(defn getWidgetName [this w]
;  "获取widget的名字"
;  (.get (.inverse widgets) w))
;
;(defn changeWidgetName [this w newName]
;  "更改widget的名字"
;  (.put (.inverse widgets) w newName))
;
;(defn getDrawList [this]
;  "获取绘制列表"
;  (ImmutableList/copyOf widgetList))
;
;(defn widgetCount [this]
;  "widget的数量"
;  (.size widgetList))
;
;(defn reorder [this target pivot]
;  "重新排序"
;  (.remove widgetList target)
;  (let [litr (.listIterator widgetList)]
;    (if-not pivot
;      (.add litr target)
;      (while (.hasNext litr)
;        (let [w (.next litr)]
;          (when (= w pivot)
;            (.add litr target)
;            (break)))))))
;
;(defn reorder [this target newIndex]
;  "重新排序"
;  (let [prevIndex (.indexOf widgetList target)]
;    (.remove widgetList prevIndex)
;    (if (> newIndex prevIndex)
;      (.add widgetList (- newIndex 1) target)
;      (.add widgetList newIndex target))))
;
;; 获取下一个未命名的widget的名字
;(defn getNextName [this]
;  "获取下一个未命名的widget的名字"
;  (loop [nameCount 0]
;    (let [res (str UNNAMED_PRE nameCount)]
;      (if (hasWidget this res)
;        (recur (inc nameCount))
;        res))))
