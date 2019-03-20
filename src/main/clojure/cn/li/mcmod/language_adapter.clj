(ns cn.li.mcmod.language-adapter
  (:import (net.minecraftforge.fml.common ILanguageAdapter FMLModContainer)
           (java.lang.reflect Method)))


(gen-class {^static: true}
  :name ClojureAdapter
  :implements [ILanguageAdapter]
  :prefix "clj-"
  ;:methods [[getNewInstance [FMLModContainer Class ClassLoader Method] Object]
  ;          [supportsStatics [] boolean]])
  )

(defn- clj-getNewInstance [this container objectClass classLoader ^Method factoryMarkedAnnotation]
  (if factoryMarkedAnnotation
    (.invoke factoryMarkedAnnotation nil)
    (.newInstance objectClass)))

(defn- clj-supportsStatics [this] true)

(defn- clj-setProxy [this target proxyTarget proxy]
  (.set target nil proxy))

(def- clj-setInternalProxies [this ])
