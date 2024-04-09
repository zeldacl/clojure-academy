;(ns cn.lambdalib2.cgui.component.ElementList
;  (:import [java.util LinkedList Collections ListIterator]
;           [cn.lambdalib2.cgui Widget]
;           [cn.lambdalib2.cgui.annotation CGuiEditorComponent]
;           [cn.lambdalib2.cgui.event GuiEvent]
;           [cn.lambdalib2.util MathUtils]
;           [com.google.common.collect ImmutableList]))
;
;;; 定义ElementList组件
;(defn ElementList []
;  (let [subWidgets (LinkedList.)]
;    (fn []
;      ;; 定义组件的属性
;      (let [spacing (atom 0.0)
;            progress (atom 0)
;            loaded (atom false)]
;        ;; 定义组件的方法
;        {:onAdded (fn [this]
;                    (reset! loaded true)
;                    (doseq [ww subWidgets]
;                      (.addWidget widget ww))
;                    (updateList))
;         :getProgress (fn [] @progress)
;         :getMaxProgress (fn []
;                           (let [sum (atom 0.0)
;                                 i (atom (- (count subWidgets) 1))]
;                             (while (>= @i 0)
;                               (let [w (.get subWidgets @i)]
;                                 (swap! sum + (.height (.transform w)))
;                                 (swap! sum + @spacing)
;                                 (if (>= @sum (.height (.transform widget)))
;                                   (if (= @i (- (count subWidgets) 1))
;                                     (do (reset! i @i)
;                                         @i)
;                                     (do (reset! i (+ @i 1))
;                                         @i))
;                                   (do (reset! i (- @i 1))
;                                       nil))))
;                             0))
;         :progressNext (fn [] (setProgress (+ @progress 1)))
;         :progressLast (fn [] (setProgress (- @progress 1)))
;         :setProgress (fn [newProgress]
;                        (let [newProgress (MathUtils/clampi 0 (getMaxProgress) newProgress)]
;                          (when (and @loaded (!= @progress newProgress))
;                            (updateList))
;                          (reset! progress newProgress)))
;         :getSubWidgets (fn [] (ImmutableList/copyOf subWidgets))
;         :size (fn [] (count subWidgets))
;         :onRemoved (fn [this]
;                      (doseq [w subWidgets]
;                        (.dispose w)))
;         :addWidget (fn [w]
;                      (preAdd w)
;                      (.add subWidgets w)
;                      (postAdd w))
;         :addWidgetAfter (fn [pivot & ws]
;                           (doseq [w ws] (preAdd w))
;                           (let [itr (.listIterator subWidgets)]
;                             (loop []
;                               (when (.hasNext itr)
;                                 (let [iw (.next itr)]
;                                   (if (= iw pivot)
;                                     (do (doseq [w ws] (.add itr w))
;                                         (recur false))
;                                     (recur true)))))
;                             (when true
;                               (doseq [w ws] (postAdd w)))))
;         :preAdd (fn [w] (.setNeedCopy w false))
;         :postAdd (fn [w]
;                    (when @loaded
;                      (.addWidget widget w)
;                      (updateList)))
;         :getFullHeight (fn []
;                          (sumHeight 0 (count subWidgets)))
;         :shouldScroll (fn []
;                         (< (.height (.transform widget))
;                           (getFullHeight)))
;         :sumHeight (fn [from to]
;                      (let [ret (atom 0.0)]
;                        (doseq [i (range from to)]
;                          (let [w (.get subWidgets i)]
;                            (swap! ret + (.height (.transform w)))
;                            (swap! ret + @spacing)))
;                        (when (= to (count subWidgets))
;                          (swap! ret - @spacing))
;                        @ret)))
;      ;; 返回组件
;      ))))
;
;;; 定义CGuiEditorComponent注解
;(defn CGuiEditorComponent [f] f)
;
;;; 定义ProgressChangedEvent事件
;(defrecord ProgressChangedEvent [this])
;
;;; 定义MathUtils/clampi函数
;(defn clampi [a b c]
;  (max a (min b c)))
;
;;; 定义updateList函数
;(defn updateList []
;  (let [sum (atom 0.0)]
;    (doseq [w subWidgets]
;      (.setDoesDraw (.transform w) false))
;    (doseq [i (range @progress (count subWidgets))
;            :let [w (.get subWidgets i)]]
;      (when (<= (+ @sum (.height (.transform w))) (.height (.transform widget)))
;        (.setDoesDraw (.transform w) true)
;        (.setX (.transform w) 0)
;        (.setY (.transform w) @sum)
;        (.setDirty w true)
;        (swap! sum + (.height (.transform w)))
;        (swap! sum + @spacing)))))
;
;;; 定义copy函数
;(defn copy [el]
;  (let [el (copy-component el)]
;    (doseq [w subWidgets]
;      (.addWidget el w))
;    el))
;(Widget w : subWidgets) {
;                         el.addWidget(w);
;                         }
;return el;
;}
;
;}
