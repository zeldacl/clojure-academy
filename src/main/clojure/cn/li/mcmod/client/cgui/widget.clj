;(ns cn.li.mcmod.client.cgui.widget)
;
;
;(ns cn.lambdalib2.cgui.Widget
;  (:import [java.util LinkedList List Iterator])
;  (:require [cn.lambdalib2.cgui.component :as component]
;            [cn.lambdalib2.cgui.event :as event]
;            [cn.lambdalib2.cgui.event :refer [GuiEvent GuiEventBus IGuiEventHandler]]))
;
;(defprotocol Component
;  (copy [this])
;  (onAdded [this])
;  (onRemoved [this]))
;
;(defprotocol Transform
;  (setSize [this width height])
;  (setPos [this x y])
;  (setCenteredAlign [this])
;  (doesDraw [this])
;  (doesListenKey [this]))
;
;(defrecord TransformImpl [width height x y alignWidth alignHeight scale doesDraw doesListenKey]
;  Transform
;  (setSize [this width height]
;    (assoc this :width width :height height))
;  (setPos [this x y]
;    (assoc this :x x :y y))
;  (setCenteredAlign [this]
;    (assoc this :alignWidth component/WidthAlign/CENTER :alignHeight component/HeightAlign/CENTER))
;  (doesDraw [this]
;    true)
;  (doesListenKey [this]
;    true))
;
;(defrecord Widget [eventBus components disposed dirty gui parent abstractParent x y scale needCopy hidden transform widgets]
;  Component
;  (copy [this]
;    (let [n (Widget.)]
;      (copyInfoTo this n)
;      n))
;  (onAdded [this])
;  (onRemoved [this]))
;
;(defn addComponent [this c]
;  (if (.widget c)
;    (throw (Exception. "Can't add one component into multiple widgets!")))
;  (assoc this :components (conj (:components this) c)
;              :transform (assoc (:transform this) :doesDraw true)
;              :dirty true
;              :eventBus (event/listen c (:eventBus this)))
;  c/onAdded c)
;
;(defn removeComponent [this c]
;  (removeComponentByClass this (.getClass c)))
;
;(defn removeComponentByClass [this klass]
;  (let [iter (iterator (:components this))]
;    (while (.hasNext iter)
;      (let [c (.next iter)]
;        (when (instance? klass c)
;          (c/onRemoved c)
;          (assoc this :components (remove c (:components this)))
;          (.remove iter))))))
;
;(defn getComponent [this type]
;  (first (filter #(instance? type %) (:components this))))
;
;(defn addComponents [this & c]
;  (reduce addComponent this c))
;
;(defn copyInfoTo [this n]
;  (let [transform (copy (:transform this))]
;    (assoc n :components (filter #(not (instance? TransformImpl %)) (:components this))
;             :transform transform
;             :eventBus (event/copy (:eventBus this))
;             :widgets (reduce #(assoc %1 %2 (.copy %2)) {} (getDrawList this)))))
;
;(defn getDrawList [this]
;  (vals (:widgets this)))
;
;(defn addWidget [this & args]
;  (let [name (if (string? (first args)) (first args) (str (count (:widgets this))))
;        widget (if (string? (first args)) (second args) (first args))]
;    (assoc this :widgets (assoc (:widgets this) name widget)
;                :dirty true
;                :eventBus (event/listen widget (:eventBus this))
;                :abstractParent (assoc (:abstractParent widget) name this))
;    widget))
;
;(defn addWidgetByName [this name widget]
;  (assoc this :widgets (assoc (:widgets this) name widget)
;              :dirty true
;              :eventBus (event/listen widget (:eventBus this))
;              :abstractParent (assoc (:abstractParent widget) name this)))
;
;(defn removeWidget [this name]
;  (let [widget (getWidgetByName this name)]
;    (when widget
;      (assoc this :widgets (dissoc (:widgets this) name)
;                  :dirty true
;                  :eventBus (event/unlisten widget (:eventBus this))
;                  :abstractParent (dissoc (:abstractParent widget) name))
;      widget/dispose)))
;
;(defn getWidgetByName [this name]
;  (get (:widgets this) name))
;
;(defn getWidgetName [this widget]
;  (first (filter #(= widget (second %)) (:widgets this))))
;
;(defn renameWidget [this oldName newName]
;  (let [widget (getWidgetByName this oldName)]
;    (when widget
;      (assoc this :widgets (assoc (dissoc (:widgets this) oldName) newName widget)
;                  :dirty true
;                  :abstractParent (assoc (dissoc (:abstractParent widget) oldName) newName this)))))
;
;
;(defn listen [this clazz & args]
;  (let [priority (if (integer? (first args)) (first args) 0)
;        copyable (if (boolean? (first args)) (first args) true)
;        handler (if (integer? (first args)) (second args) (first args))]
;    (assoc this :eventBus (event/listen clazz handler priority copyable (:eventBus this)))))
;
;(defn unlisten [this clazz handler]
;  (assoc this :eventBus (event/unlisten clazz handler (:eventBus this))))
;
;(defn post [this event & [tochild]]
;  (event/postEvent (:eventBus this) this event)
;  (when tochild
;    (doseq [w (getDrawList this)]
;      (post w event true))))
;
;(defn isVisible [this]
;  (and (doesDraw (:transform this)) (not (:hidden this))))
;
;(defn copy [this]
;  (copyInfoTo this (Widget.)))
;
;(defn dispose [this]
;  (assoc this :disposed true))
;
;(defn initialized [this]
;  (not (nil? (:gui this))))
;
;(defn isWidgetParent [this]
;  (not (nil? (:parent this))))
;
;(defn getWidgetParent [this]
;  (:parent this))
;
;(defn getGui [this]
;  (:gui this))
;
;(defn isPointWithin [this tx ty]
;  (let [w (:width (:transform this))
;        h (:height (:transform this))
;        x1 (+ (:x this) (* w (:scale this)))
;        y1 (+ (:y this) (* h (:scale this)))]
;    (and (<= (:x this) tx (< tx x1))
;      (<= (:y this) ty (< ty y1)))))
;
;(defn isFocused [this]
;  (and (:gui this) (= this (.getFocus (:gui this)))))
;
;(defn markDirty [this]
;  (assoc this :dirty true))
;
;(defn getHierarchyLevel [this]
;  (loop [ret 0 cur this]
;    (if (isWidgetParent cur)
;      (recur (inc ret) (.getWidgetParent cur))
;      ret)))
;
;(defn getAbstractParent [this]
;  (:abstractParent this))
;
;(defn rename [this newName]
;  (let [parent (getAbstractParent this)]
;    (when (not (getWidgetByName parent newName))
;      (renameWidget parent (getWidgetName parent this) newName)
;      true)))
;
;(defn isChildOf [this another]
;  (loop [cur this]
;    (if (nil? cur)
;      false
;      (if (= cur another)
;        true
;        (recur (.getWidgetParent cur))))))
;
;(defn gainFocus [this]
;  (.gainFocus (:gui this) this))
;
;(defn toString [this]
;  (str (getName this) "@" (-> this .getClass .getSimpleName)))
;
;(defn getHierarchyStructure [this]
;  (getHierarchyStructureInt this #(str (getName %) "@" (-> % .getClass .getSimpleName)) 0))
;
;(defn getHierarchyStructurePos [this]
;  (getHierarchyStructureInt this #(str (getName %) ": (" (:x (:transform %)) "," (:y (:transform %)) ")[" (:width (:transform %)) "," (:height (:transform %)) "]x" (:scale (:transform %))) 0))
;
;(defn getHierarchyStructureInt [this f indent]
;  (let [istr (apply str (repeat (* 2 indent) " "))]
;    (str istr (f this)
;      (if (empty? (getDrawList this))
;        ""
;        (str istr "{\n" (apply str (map #(getHierarchyStructureInt % f (inc indent)) (getDrawList this))) istr "}\n")))))
;w.transform.scale));
;}
;(defn gainFocus [this]
;  (.gainFocus (:gui this) this))
;public String getHierarchyStructure(IWidgetFormatter f) {
;                                                         public String getHierarchyStructure(IWidgetFormatter f) {
;                                                                                                                  return getHierarchyStructure_int(f, 0);
;                                                                                                                  }
;                                                         (defn toString [this]
;                                                           (str (getName this) "@" (-> this .getClass .getSimpleName)))
;                                                         private String _rep(int times) {
;                                                                                         private String _rep(int times) {
;                                                                                                                         StringBuilder sb = new StringBuilder(times*2);
;                                                                                                                         while (times-- > 0) sb.append("  ");
;                                                                                                                         return sb.toString();
;                                                                                                                         }
;                                                                                         (defn getHierarchyStructure [this]
;                                                                                           (getHierarchyStructureInt this #(str (getName %) "@" (-> % .getClass .getSimpleName)) 0))
;                                                                                         private String getHierarchyStructure_int(IWidgetFormatter f, int indent) {
;                                                                                                                                                                   String istr = _rep(indent);
;                                                                                                                                                                   StringBuilder ret = new StringBuilder();
;                                                                                                                                                                   ret.append(istr).append(f.format(this));
;                                                                                                                                                                   if(getDrawList().size() != 0) {
;                                                                                                                                                                                                  ret.append(istr).append("{\n");
;                                                                                                                                                                                                  getDrawList().forEach(w -> ret.append(w.getHierarchyStructure_int(f, indent + 1)).append(','));
;                                                                                                                                                                                                                         ret.append(istr).append("\n}");
;                                                                                                                                                                                                  }
;                                                                                                                                                                   return ret.toString();
;                                                                                                                                                                   }
;
;                                                                                         }
;
