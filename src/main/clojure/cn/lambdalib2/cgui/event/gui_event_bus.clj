(ns cn.lambdalib2.cgui.event.gui-event-bus
  (:require [net.minecraftforge.fml.relauncher :refer [SideOnly]]
            [net.minecraftforge.common :refer [MinecraftForge]]))

(defrecord GuiHandlerNode [handler priority copy-sensitive])

(defrecord NodeCollection [toadd toremove iterating])

(defn- priority-cmp [n1 n2]
  (- (:priority n2) (:priority n1)))

(defn- get-raw-list [event-handlers clazz]
  (or (get event-handlers clazz)
      (assoc event-handlers clazz (NodeCollection. [] [] false))))

(defn post-event [event-handlers widget event]
  (let [list (get event-handlers (class event))]
    (when list
      (swap! list assoc :iterating true)
      (doseq [n (:toadd list)]
        ((:handler n) widget event))
      (swap! list assoc :iterating false)
      (swap! list update :toadd empty)
      (swap! list update :toremove empty))))

(defn listen [event-handlers clazz handler & [priority copyable]]
  (let [priority (or priority 0)
        copyable (or copyable true)
        list (get-raw-list event-handlers clazz)]
    (when-not (some #(= handler (:handler %)) (:toadd list))
      (swap! list update :toadd conj (GuiHandlerNode. handler priority copyable)))))

(defn unlisten [event-handlers clazz handler]
  (let [list (get-raw-list event-handlers clazz)]
    (swap! list update :toremove conj (GuiHandlerNode. handler 0 false))))

(defn copy [event-handlers]
  (reduce (fn [acc [k v]]
            (assoc acc k (NodeCollection. (filter :copy-sensitive (:toadd v)) [] false)))
          {} event-handlers))