(ns cn.academy.client.render.cat-engine-renderer
  (:require [cn.academy.protocols.render :as render-api]
            [cn.academy.block.tileentity.tile-cat-engine :as tile-engine]
            [cn.lambdalib2.util.game-timer :as game-timer])
  (:import [org.lwjgl.opengl GL11]))

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

(defn render-cat-engine [tile-entity x y z pt destroy-stage alpha]
  (let [time (long (* (game-timer/get-time) 1000))
        engine tile-entity]
    (when (not= (tile-engine/get-last-render engine) 0)
      (let [rotation-delta (* (- time (tile-engine/get-last-render engine))
                            (tile-engine/get-this-tick-gen engine)
                            1e-2)
            new-rotation (mod (+ (tile-engine/get-rotation engine) rotation-delta) 360)]
        (tile-engine/set-rotation! engine new-rotation)))
    
    (tile-engine/set-last-render! engine time)
    
    (render-api/with-gl-state
      ; Setup GL state
      (GL11/glDisable GL11/GL_CULL_FACE)
      (GL11/glTranslated (+ x 0.5) 
                        (+ y (* 0.03 (Math/sin (* (game-timer/get-time) 0.006))))
                        (+ z 0.5))
      
      ; Render the model
      (render-api/render-block-model "cljacademy:block/cat_engine" 
                                    "cljacademy:textures/block/cat_engine.png"))))

(defn register []
  (render-api/register-tile-entity-renderer 
    :cat-engine 
    render-cat-engine))