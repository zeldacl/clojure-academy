(ns cn.li.mcmod.client.cgui.core)

(defn breathe-alpha []
  (let [time (System/currentTimeMillis)
        sin (* (+ 1 (Math/sin (/ time 0.8))) 0.5)]
    (+ 0.675 (* sin 0.175))))

(defmacro defcomponent [name fields]
  (let [fields (conj fields 'widget 'name)]
    `(defrecord ~name ~fields)))


(defcomponent ComponentTransform [pos size])


;(defrecord Component [])

(defmacro defwidget [name fields]
  (let [fields (conj fields 'transform 'name)]
    `(defrecord ~name ~fields)))
;(defrecord Widget [])

(defprotocol Render
  (render [this]))
