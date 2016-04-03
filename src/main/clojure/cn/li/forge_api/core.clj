(ns cn.li.forge-api.core
  )


(defn gen-classname [s]
  (let [s (str s)
        words (clojure.string/split s #"-")
        class-name (apply str (map clojure.string/capitalize words))]
    (symbol class-name)))

(defn gen-method [k]
  (let [key-name (name k)
        words (clojure.string/split key-name #"-")
        method-name (apply str (first words) (map clojure.string/capitalize (rest words)))]
    (symbol method-name)))

(defn gen-setter [k]
  (symbol (str "." (gen-method (str "set-" (name k))))))

(defmacro defclass [class-name classdata]
  (let [interfaces (or (:interfaces classdata) [])
        fields (or (:fields classdata) {})
        extends (or (:extends classdata) Object)
        exposes-methods (or (:exposes-methods classdata) {})
        init-fn (:init-fn classdata)
        post-init-fn (get classdata :post-init-fn `(constantly nil))
        prefix (str class-name "-")
        fullname (symbol (str (clojure.string/replace (str *ns*) #"-" "_") "." (gen-classname class-name)))]

    `(do
       (gen-class
         :name ~fullname
         :extends ~extends
         :prefix ~prefix
         :implements ~interfaces
         :exposes-methods ~exposes-methods
         :state ~'state
         :init ~'init
         :post-init ~'post-init)
       (def ~class-name ~fullname)
       (defn ~(symbol (str prefix "init"))
         ~(if init-fn
            init-fn
            `([]
               [[] (atom ~fields)])))
       (defn ~(symbol (str prefix "post-init"))
         ([~'this]
           nil)
         ([~'this & ~'opts]
           ;(~post-init-fn ~'this ~'opts)
           ))
       (defn ~(symbol (str prefix "deref")) [~'this]
         (~'.state ~'this)))))
