(ns cn.li.mcmod.client.cgui.core)

(defmacro defcomponent [name fields]
  (let [fields (conj fields 'widget)]
    `(defrecord ~name ~fields)))

;(defrecord Component [])

(defrecord Widget [])

(defprotocol Render
  (render [this]))
