(ns cn.lambdalib2.cgui.component.component
  (:require [net.minecraftforge.fml.relauncher :refer [SideOnly]]
            [cn.lambdalib2.s11n :refer [CopyHelper]]))

(defrecord Component [name enabled can-edit widget added-handlers])

(defn- create-component [name]
  (Component. name true true nil []))

(defn listen [component type handler & [prio]]
  (let [prio (or prio 0)]
    (if (:widget component)
      (throw (RuntimeException. "Can only add event handlers before component is added into widget"))
      (let [n {:type type :handler (EHWrapper. handler) :prio prio}]
        (swap! (:added-handlers component) conj n)))))

(defn on-added [component]
  (doseq [n (:added-handlers component)]
    (widget/listen (:widget component) (:type n) (:prio n) false (:handler n))))

(defn on-removed [component]
  (doseq [n (:added-handlers component)]
    (widget/unlisten (:widget component) (:type n) (:handler n))))

(defn copy [component]
  (CopyHelper/instance/copy component))

(defrecord EHWrapper [wrapped]
  IGuiEventHandler
  (handleEvent [this widget event]
    (when (:enabled component)
      (.handleEvent (:wrapped this) widget event))))

(defrecord Node [type handler prio])