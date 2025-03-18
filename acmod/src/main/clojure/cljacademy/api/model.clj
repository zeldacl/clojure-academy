(ns cljacademy.api.model
  (:require [clojure.java.io :as io]))

(defprotocol IModelLoader
  (load-model [this path])
  (load-texture [this path])
  (register-model [this model-id model])
  (register-texture [this texture-id texture]))

(defprotocol IModel
  (render-part [this part-name])
  (get-texture [this])
  (set-texture [this texture]))

(def ^:dynamic *model-loader* nil)

(defn with-model-loader [loader f]
  (binding [*model-loader* loader]
    (f)))

(defn load-obj-model [path]
  (when *model-loader*
    (load-model *model-loader* path)))

(defn load-model-texture [path]
  (when *model-loader*
    (load-texture *model-loader* path)))

(defn register-model! [model-id model]
  (when *model-loader*
    (register-model *model-loader* model-id model)))

(defn register-texture! [texture-id texture]
  (when *model-loader*
    (register-texture *model-loader* texture-id texture)))