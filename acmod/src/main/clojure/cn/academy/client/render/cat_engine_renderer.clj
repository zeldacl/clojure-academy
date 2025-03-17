(ns cn.academy.client.render.cat-engine-renderer
  (:require [cn.academy.api.render :as render-api]))

(defprotocol ICatEngineRenderer
  (render [this tile-entity x y z partial-ticks destroyed-stage])
  (get-model [this])
  (get-texture [this]))

(defrecord CatEngineRenderer [properties]
  ICatEngineRenderer
  (render [_ tile-entity x y z partial-ticks destroyed-stage]
    (let [model (get-model _)
          texture (get-texture _)]
      (render-api/with-push-matrix
        (render-api/translate x y z)
        (render-api/bind-texture texture)
        (render-api/render-model model))))
  
  (get-model [_]
    (:model properties))
  
  (get-texture [_]
    (:texture properties)))

(defn create []
  (->CatEngineRenderer
    {:model (render-api/load-model "academy:models/cat_engine.json")
     :texture (render-api/load-texture "academy:textures/blocks/cat_engine.png")}))