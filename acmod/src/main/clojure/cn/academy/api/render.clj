(ns cn.academy.api.render
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

(def ^:dynamic *render-impl* nil)
(def ^:dynamic *model-loader* nil)

(defmacro with-push-matrix [& body]
  `(try
     (.pushMatrix ^Object (:matrix-stack *render-impl*))
     ~@body
     (finally
       (.popMatrix ^Object (:matrix-stack *render-impl*)))))

(defn translate [x y z]
  ((:translate *render-impl*) x y z))

(defn bind-texture [texture]
  ((:bind-texture *render-impl*) texture))

(defn render-model [model]
  ((:render-model *render-impl*) model))

;; Model loading functions
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

;; Implementation setters
(defn set-render-impl! [impl]
  (alter-var-root #'*render-impl* (constantly impl)))

(defn set-model-loader! [loader]
  (alter-var-root #'*model-loader* (constantly loader)))