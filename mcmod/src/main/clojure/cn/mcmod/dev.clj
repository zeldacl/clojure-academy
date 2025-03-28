(ns cn.mcmod.dev
  (:require [cn.mcmod.protocols :refer :all]
            [cn.mcmod.registry :as registry]
            [clojure.pprint :refer [pprint]]))

(defn debug-print [& args]
  (when (System/getProperty "mcmod.debug")
    (apply println args)))

(defn inspect-registry [registry]
  (debug-print "\nRegistry Contents:")
  (debug-print "Blocks:")
  (pprint (registry/get-blocks registry))
  (debug-print "\nItems:")
  (pprint (registry/get-items registry))
  (debug-print "\nTile Entities:")
  (pprint (registry/get-tile-entities registry)))

(defmacro with-dev-context [& body]
  `(try
     (System/setProperty "mcmod.debug" "true")
     ~@body
     (finally
       (System/setProperty "mcmod.debug" "false"))))