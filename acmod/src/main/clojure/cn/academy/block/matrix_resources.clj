(ns cn.academy.block.matrix-resources
  (:require [clojure.java.io :as io]))

(defprotocol IMatrixResources
  (load-model [this])
  (load-texture [this])
  (get-model-location [this])
  (get-texture-location [this])
  (register-resources [this]))

(defrecord MatrixResourceManager [mod-id]
  IMatrixResources
  (load-model [_]
    {:location (str mod-id ":models/block/matrix.json")
     :data {:parent "block/cube_all"
            :textures {"all" (str mod-id ":blocks/matrix")}}})
  
  (load-texture [_]
    {:location (str mod-id ":textures/blocks/matrix.png")
     :data (io/resource "assets/academy/textures/blocks/matrix.png")})
  
  (get-model-location [_]
    (str mod-id ":block/matrix"))
  
  (get-texture-location [_]
    (str mod-id ":blocks/matrix"))
  
  (register-resources [this]
    (load-model this)
    (load-texture this)))

(defn create-resource-manager [mod-id]
  (->MatrixResourceManager mod-id))