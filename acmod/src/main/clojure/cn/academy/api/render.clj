(ns cn.academy.api.render)

(def ^:dynamic *render-impl* nil)

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

(defn load-model [path]
  ((:load-model *render-impl*) path))

(defn load-texture [path]
  ((:load-texture *render-impl*) path))

(defn set-render-impl! [impl]
  (alter-var-root #'*render-impl* (constantly impl)))