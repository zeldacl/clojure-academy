;(ns cn.lambdalib2.cgui.component.drag-bar
;  (:require [cn.lambdalib2.cgui.widget :as widget]
;            [cn.lambdalib2.cgui.event.drag :as drag]
;            [cn.lambdalib2.cgui.event.gui :as gui]
;            [cn.lambdalib2.util.math :as math]))
;
;(defn- clampf [^float x ^float a ^float b]
;  "将x限制在a和b之间"
;  (if (< x a) a (if (> x b) b x)))
;
;(defn- dragged-event []
;  "拖拽事件"
;  [])
;
;(defn- get-progress [^widget.Widget w ^:keyword axis ^float lower ^float upper]
;  "获取进度"
;  (let [ret (if (= axis :x)
;              (/ (- (.transform w) :y lower) (- upper lower))
;              (/ (- (.transform w) :x lower) (- upper lower)))]
;    (clampf (if (< ret 0) 0 (if (> ret 1) 1 ret)))))
;
;(defn- set-progress [^widget.Widget w ^:keyword axis ^float lower ^float upper ^float prg]
;  "设置进度"
;  (let [val (+ lower (* (- upper lower) prg))]
;    (if (= axis :x)
;      (set! (.transform w) (widget/transform w :x val))
;      (set! (.transform w) (widget/transform w :y val)))
;    (set! (.dirty w) true)))
;
;(defn- set-area [^drag-bar.DragBar this ^float lower ^float upper]
;  "设置拖拽区域"
;  (set! (.-lower this) lower)
;  (set! (.-upper this) upper)
;  this)
;
;(defn- listen-drag-event [^drag-bar.DragBar this]
;  "监听拖拽事件"
;  (gui/listen this drag/DragEvent
;    (fn [^widget.Widget w event]
;      (let [original (if (= (.-axis this) :x)
;                       (.transform w :y)
;                       (.transform w :x))]
;        (.updateDragWidget (.-gui w))
;        (if (= (.-axis this) :x)
;          (do
;            (set! (.transform w) (widget/transform w :y original))
;            (set! (.transform w) (widget/transform w :x (clampf lower upper (.transform w :x)))))
;          (do
;            (set! (.transform w) (widget/transform w :x original))
;            (set! (.transform w) (widget/transform w :y (clampf lower upper (.transform w :y))))))
;        (.updateWidget (.-gui w) w)
;        (.post w (dragged-event))))))
;
;(defn- drag-bar []
;  "拖拽条"
;  (let [this (.-DragBar (new drag-bar.DragBar))]
;    (listen-drag-event this)
;    this))
;
;(defn- drag-bar-with-args [^:keyword axis ^float lower ^float upper]
;  "带参数的拖拽条"
;  (let [this (.-DragBar (new drag-bar.DragBar axis lower upper))]
;    (listen-drag-event this)
;    this))
;
;(defn- get-drag-bar [^widget.Widget w]
;  "获取拖拽条"
;  (widget/get-component w drag-bar.DragBar))
;
;(defn- drag-bar-component []
;  "拖拽条组件"
;  (widget/component "DragBar" #'drag-bar))
